package fr.akai.common;

import com.bitwig.extension.controller.api.*;
import fr.akai.MPK249Extension;


public class MPK2Hardware {
    private final MPK249Extension extension;
    private final HardwareSurface hardwareSurface;
    private final ControllerHost host;
    private final CursorRemoteControlsPage remoteControls;

    // --- ÉTATS LOGIQUES ---
    private final SettableBooleanValue isShiftActive;

    // --- PARAMÈTRES MATÉRIEL ---
    public static final int[] BUTTON_CCS = { 28, 29, 30, 31, 32, 33, 34, 35, 75, 76, 77, 78, 79, 80, 81, 82, 106, 107, 108, 109, 110, 111, 112, 113 };
    public static final int[] KNOB_CCS   = { 12, 13, 14, 15, 16, 17, 18, 19, 52, 53, 54, 55, 56, 57, 58, 59, 84, 85, 86, 87, 88, 89, 90, 91 };
    public static final int[] FADER_CCS  = { 20, 21, 22, 23, 24, 25, 26, 27, 67, 68, 69, 70, 71, 72, 73, 74, 92, 93, 94, 95, 96, 97, 98, 99 };

    // --- OBJETS HARDWARE ---
    private final AbsoluteHardwareKnob[] knobs = new AbsoluteHardwareKnob[24];
    private final HardwareSlider[] faders = new HardwareSlider[24];
    private final HardwareButton[] buttons = new HardwareButton[24];
    private final HardwareButton[] pads = new HardwareButton[64];
    private HardwareButton btnUp, btnDown, btnLeft, btnRight, btnFootRec, btnShift;

    public TrackBank knobsTrackBank;

    public MPK2Hardware(MPK249Extension extension, CursorRemoteControlsPage remoteControls) {
        this.extension = extension;
        this.remoteControls = remoteControls;
        this.host = extension.getHost();
        this.hardwareSurface = host.createHardwareSurface();
        this.knobsTrackBank = host.createTrackBank(8, 0, 0);

        // Création d'un état booléen interne via DocumentState
        this.isShiftActive = host.getDocumentState().getBooleanSetting("Shift Active", "Internal State", false);

        initPhysicalControls();
        initPads();
        setupLogic();
        setupNavigationAndFoot();
    }

    private void initPhysicalControls() {
        // On utilise la méthode renommée dans ton Extension (getMidiIn au lieu de getMidiInPort)
        MidiIn midiIn = extension.getMidiIn();

        for (int i = 0; i < 24; i++) {
            // Knobs
            knobs[i] = hardwareSurface.createAbsoluteHardwareKnob("KNOB_" + i);
            knobs[i].setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, KNOB_CCS[i]));

            // Faders
            faders[i] = hardwareSurface.createHardwareSlider("FADER_" + i);
            faders[i].setAdjustValueMatcher(midiIn.createAbsoluteCCValueMatcher(0, FADER_CCS[i]));

            // Boutons S1-S24
            buttons[i] = hardwareSurface.createHardwareButton("BUTTON_" + i);
            buttons[i].pressedAction().setActionMatcher(midiIn.createCCActionMatcher(0, BUTTON_CCS[i], 127));
            buttons[i].releasedAction().setActionMatcher(midiIn.createCCActionMatcher(0, BUTTON_CCS[i], 0));
        }

        // Navigation & Foot
        btnUp = createCCButton("BTN_UP", 62);
        btnDown = createCCButton("BTN_DOWN", 63);
        btnLeft = createCCButton("BTN_LEFT", 60);
        btnRight = createCCButton("BTN_RIGHT", 61);
        btnShift = createCCButton("BTN_SHIFT", 65);


        btnFootRec = createToggleCCButton("FOOT_REC", 64);
    }

    private void initPads() {
        MidiIn midiIn = extension.getMidiIn();
        for (int i = 0; i < 64; i++) {
            int note = 36 + i;

            // On crée le bouton normalement sur la surface
            pads[i] = hardwareSurface.createHardwareButton("PAD_" + i);

            // On définit les actions (clic)
            pads[i].pressedAction().setActionMatcher(midiIn.createNoteOnActionMatcher(9, note));
            pads[i].releasedAction().setActionMatcher(midiIn.createNoteOffActionMatcher(9, note));
            }
    }

    private void setupLogic() {

        // ON MARQUE TOUT AU DÉBUT (Phase Init)
        for (int i = 0; i < 8; i++) {
            Track track = knobsTrackBank.getItemAt(i);
            track.volume().markInterested(); // Indispensable ici !
            track.pan().markInterested();    // Indispensable ici !
        }

        // --- SHIFT LOGIC (S8 = CC 35) ---
        // On met à jour l'état et on déclenche la re-liaison des potards
        btnShift.isPressed().addValueObserver(pressed -> {
            if (pressed) {
                host.showPopupNotification("shift is pressed (pan control)");
            } else {
            host.showPopupNotification("shift released (vol control)");
            }
            isShiftActive.set(pressed);
            updateKnobBindings(pressed);
        });

        // --- PREMIER BINDING AU DÉMARRAGE ---
        updateKnobBindings(false);

        // --- BINDINGS FADERS (BANK A) ---
        for (int i = 0; i < 8; i++) {
            faders[i].setBinding(remoteControls.getParameter(i));
            host.println(remoteControls.pageNames().toString());
        }
        //ancien binding au volume pour les faders
//        for (int i = 0; i < 8; i++) {
//            faders[i].setBinding(knobsTrackBank.getItemAt(i).volume());
//        }

        // --- FOLLOW CURSOR LOGIC ---
        extension.getCursorTrack().position().addValueObserver(pos -> {
            if (extension.getFollowCursorSetting().get() && pos >= 0) {
                knobsTrackBank.scrollPosition().set((pos / 8) * 8);
            }
        });
    }

    // Méthode pour basculer dynamiquement entre Volume et Pan
    private void updateKnobBindings(boolean shift) {
        for (int i = 0; i < 8; i++) {
            Track track = knobsTrackBank.getItemAt(i);

             if (shift) {
                knobs[i].setBinding(track.pan());
            } else {
                knobs[i].setBinding(track.volume());
            }
        }
    }

    private void setupNavigationAndFoot() {
        CursorTrack cursorTrack = extension.getCursorTrack();
        CursorDevice cursorDevice = extension.getCursorDevice();
        CursorClip cursorClip = cursorTrack.createLauncherCursorClip(1, 1);
        ClipLauncherSlotBank slotBank = cursorTrack.clipLauncherSlotBank();

        // Navigation controls (track / clip / device selection bindings)
        // --- bind during first init
        HardwareActionBindable selectNextClip = cursorClip.selectNextAction();
        HardwareActionBindable selectPrevClip = cursorClip.selectPreviousAction();
        // navigation logic
        btnUp.pressedAction().setBinding(cursorTrack.selectPreviousAction());
        btnDown.pressedAction().setBinding(cursorTrack.selectNextAction());
        btnLeft.pressedAction().addBinding(host.createAction(() -> {
            if (isShiftActive.get()) {
                cursorDevice.selectPrevious(); // Shift + Left = Device précédent
                host.showPopupNotification("Device: Previous");
            } else {
                selectPrevClip.invoke(); // Left seul = Clip précédent
            }
        }, () -> "Navigate Left (Clip or Device)"));

        btnRight.pressedAction().addBinding(host.createAction(() -> {
            if (isShiftActive.get()) {
                cursorDevice.selectNext(); // Shift + Right = Device suivant
                host.showPopupNotification("Device: Next");
            } else {
                selectNextClip.invoke(); // Right seul = Clip suivant
            }
        }, () -> "Navigate Right (Clip or Device)"));

        // --- Mix navigation (Akai banks B & C) ---
        for (int i = 0; i < 8; i++) {
            // On récupère la piste correspondante dans ta TrackBank de 8
            Track track = knobsTrackBank.getItemAt(i);
            track.position().markInterested();

            // On pré-déclare les actions de mixage pour chaque piste
            HardwareActionBindable muteAction = track.mute().toggleAction();
            HardwareActionBindable soloAction = track.solo().toggleAction();
            HardwareActionBindable armAction = track.arm().toggleAction();

            // --- BANQUE B (S9-S16) : MUTE & SOLO ---
            // L'indice 8 dans BUTTON_CCS correspond au premier bouton de la Banque B
            int finalI = i;

            buttons[8 + i].pressedAction().addBinding(host.createAction(() -> {
                int globalTrackNum = track.position().get() + 1;
                if (isShiftActive.get()) {
                    soloAction.invoke(); // Shift enfoncé = Solo
                    host.showPopupNotification("Track " + globalTrackNum + ": SOLO");
                } else {
                    muteAction.invoke(); // Bouton seul = Mute
                    host.showPopupNotification("Track " + globalTrackNum + ": MUTE");
                }
            }, () -> "Track " + (finalI + 1) + " Mute/Solo Control"));

            // --- BANQUE C (S17-S24) : RECORD ARM ---
            // L'indice 16 correspond à la Banque C. Pas de Shift ici pour plus de sécurité.
            //buttons[16 + i].pressedAction().setBinding(armAction);

            buttons[16 + i].pressedAction().addBinding(host.createAction(() -> {
                int globalTrackNum = track.position().get() + 1;
                armAction.invoke(); // Shift enfoncé = Solo
                host.showPopupNotification("Track " + globalTrackNum + ": REC");
            }, () -> "Track " + (finalI + 1) + " Track Rec Control"));
        }

        // --- Footswitch logic

        // Marquage d'intérêt pour le Footswitch

        for (int i = 0; i < 8; i++) {

            slotBank.getItemAt(i).hasContent().markInterested();

            slotBank.getItemAt(i).isRecording().markInterested();

        }
        btnFootRec.pressedAction().addBinding(host.createAction(() -> {
            int nextFree = -1;
            int recordingIdx = -1;
            for (int i = 0; i < 8; i++) {
                if (slotBank.getItemAt(i).isRecording().get()) recordingIdx = i;
                if (nextFree == -1 && !slotBank.getItemAt(i).hasContent().get()) nextFree = i;
            }
            if (recordingIdx != -1) slotBank.getItemAt(recordingIdx).launch();
            else if (nextFree != -1) cursorTrack.recordNewLauncherClip(nextFree);
        }, () -> "Foot Pedal Smart Record"));
    }

    private HardwareButton createCCButton(String id, int cc) {
        HardwareButton btn = hardwareSurface.createHardwareButton(id);
        MidiIn midiIn = extension.getMidiIn();
        btn.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(0, cc, 127));
        btn.releasedAction().setActionMatcher(midiIn.createCCActionMatcher(0, cc, 0));
        return btn;
    }
    private HardwareButton createToggleCCButton(String id, int cc) {
        HardwareButton btn = hardwareSurface.createHardwareButton(id);
        MidiIn midiIn = extension.getMidiIn();

        // On utilise createAbsoluteCCValueMatcher pour accepter 0 ET 127 comme un "clic"[cite: 4]
        btn.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(0, cc));

        return btn;
    }

    // --- GETTERS ---
    public HardwareButton getPad(int index) { return pads[index]; }
    public HardwareButton getButton(int index) { return buttons[index]; }
    public SettableBooleanValue getShiftState() { return isShiftActive; }
}