/*
 * ==============================================================================
 * Akai MPK249 Advanced Bitwig Integration
 * ==============================================================================
 * Author: sebj84
 * Repository: https://github.com/sebj84/MPK249-Bitwig
 *
 * LICENSE: MIT License
 * Copyright (c) 2024 sebj84
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS-IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * SUPPORT THE PROJECT:
 * If this script improves your workflow, consider supporting the development!
 * Buy Me a Coffee: https://ko-fi.com/sebj84
 *
 * IMPORTANT: This script is provided as-is. The author cannot be held liable
 * for any issues or damage that may affect your controller or system.
 * ==============================================================================
 */
package fr.akai;

import fr.akai.common.MPK2Common;
import fr.akai.common.MPK2Common.PadColor;
import fr.akai.common.MPK2Hardware;
import fr.akai.mode.PadMode;
import fr.akai.mode.PadInstrument;
import fr.akai.mode.PadClipLauncher;
import fr.akai.mode.PadSceneLauncher;

import com.bitwig.extension.controller.ControllerExtension;
import com.bitwig.extension.controller.api.*;

public class MPK249Extension extends ControllerExtension {
    public ControllerHost host;
    private SettableBooleanValue followCursorSetting;
    private TrackBank trackBank;
    private SceneBank sceneBank;
    private CursorTrack cursorTrack;
    private CursorDevice cursorDevice;
    private CursorRemoteControlsPage remoteControls;
    private NoteInput padNoteInput;
    private Transport transport;

    private MPK2Common common;
    private MPK2Hardware hardware;

    public MidiIn midiIn;
    public MidiOut midiOut;

    public PadMode instrumentMode;
    public PadMode clipMode;
    public PadMode sceneMode;
    public PadMode activeMode;

    //    private Application application;

    public MPK249Extension(MPK249ExtensionDefinition definition, ControllerHost host) {
        super(definition, host);
    }

    @Override
    public void init() {
        host = getHost();
        host.println("MPK249 Extension — Initializing...");

        common = new MPK2Common();
        common.attachHost(host);

        followCursorSetting = host.getPreferences().getBooleanSetting("Follow Selected Track", "Navigation", true);

        initBitwigObjects();
        initMidi();

        // Initialisation Hardware
        this.hardware = new MPK2Hardware(this, remoteControls);

        initModes();
        initObservers();

        setActiveMode(instrumentMode, "Instrument Mode (pads are actives)");
        host.showPopupNotification("MPK249 Ready");
    }

    private void initBitwigObjects() {
//        application = host.createApplication();

        // On crée la banque de 4 pistes
        trackBank = host.createMainTrackBank(4, 2, 16);
        trackBank.setShouldShowClipLauncherFeedback(true);
        trackBank.scrollPosition().markInterested();

        // --- AJOUT CRUCIAL : Activer l'indication pour les banques de clips ---
        for (int i = 0; i < 4; i++) {
            Track track = trackBank.getItemAt(i);

            ClipLauncherSlotBank slotBank = track.clipLauncherSlotBank();

            slotBank.setSkipDisabledItems(true); // Optionnel, mais aide à la stabilité
        }

        sceneBank = host.createSceneBank(64);
        // Indispensable pour que le SceneLauncher reçoive aussi les infos
        sceneBank.setIndication(true);

        cursorTrack = host.createCursorTrack(4, 16);
        cursorDevice = cursorTrack.createCursorDevice("Primary", "Primary Device", 0, CursorDeviceFollowMode.FOLLOW_SELECTION);
        remoteControls = cursorDevice.createCursorRemoteControlsPage(8);
        //debug
        //host.println(remoteControls.getName().toString());

        transport = host.createTransport();
    }

    private void initMidi() {
        midiIn = host.getMidiInPort(0);
        midiIn.setMidiCallback(this::handleMidi);
        midiIn.setSysexCallback(this::onSysex);

        NoteInput[] noteInputs = new NoteInput[16];

        // --- TES NOTE INPUTS ORIGINAUX (LISIBLES) ---
        noteInputs[0]  = midiIn.createNoteInput("MPK249 (CH1)",  "80????", "90????", "A0????", "B001??", "B00B??", "D0????", "E0????");
        noteInputs[1]  = midiIn.createNoteInput("MPK249 (CH2)",  "81????", "91????", "A1????", "B101??", "B10B??", "D1????", "E1????");
        noteInputs[2]  = midiIn.createNoteInput("MPK249 (CH3)",  "82????", "92????", "A2????", "B201??", "B20B??", "D2????", "E2????");
        noteInputs[3]  = midiIn.createNoteInput("MPK249 (CH4)",  "83????", "93????", "A3????", "B301??", "B30B??", "D3????", "E3????");
        noteInputs[4]  = midiIn.createNoteInput("MPK249 (CH5)",  "84????", "94????", "A4????", "B401??", "B40B??", "D4????", "E4????");
        noteInputs[5]  = midiIn.createNoteInput("MPK249 (CH6)",  "85????", "95????", "A5????", "B501??", "B50B??", "D5????", "E5????");
        noteInputs[6]  = midiIn.createNoteInput("MPK249 (CH7)",  "86????", "96????", "A6????", "B601??", "B60B??", "D6????", "E6????");
        noteInputs[7]  = midiIn.createNoteInput("MPK249 (CH8)",  "87????", "97????", "A7????", "B701??", "B70B??", "D7????", "E7????");
        noteInputs[8]  = midiIn.createNoteInput("MPK249 (CH9)",  "88????", "98????", "A8????", "B801??", "B80B??", "D8????", "E8????");

        // PADS → Canal 10
        padNoteInput = midiIn.createNoteInput("MPK249 Pads", "89????", "99????", "D9????", "E9????", "A9????");
        noteInputs[9]  = padNoteInput;

        noteInputs[10] = midiIn.createNoteInput("MPK249 (CH11)", "8A????", "9A????", "AA????", "BA01??", "BA0B??", "DA????", "EA????");
        noteInputs[11] = midiIn.createNoteInput("MPK249 (CH12)", "8B????", "9B????", "AB????", "BB01??", "BB0B??", "DB????", "EB????");
        noteInputs[12] = midiIn.createNoteInput("MPK249 (CH13)", "8C????", "9C????", "AC????", "BC01??", "BC0B??", "DC????", "EC????");
        noteInputs[13] = midiIn.createNoteInput("MPK249 (CH14)", "8D????", "9D????", "AD????", "BD01??", "BD0B??", "DD????", "ED????");
        noteInputs[14] = midiIn.createNoteInput("MPK249 (CH15)", "8E????", "9E????", "AE????", "BE01??", "BE0B??", "DE????", "EE????");
        noteInputs[15] = midiIn.createNoteInput("MPK249 (CH16)", "8F????", "9F????", "AF????", "BF01??", "BF0B??", "DF????", "EF????");

        for (NoteInput ni : noteInputs) ni.setShouldConsumeEvents(false);

        Integer[] table = java.util.Arrays.stream(common.getPadMidiTableOn()).boxed().toArray(Integer[]::new);
        padNoteInput.setKeyTranslationTable(table);
    }

    private void initModes() {
        instrumentMode = new PadInstrument(common, host);
        clipMode = new PadClipLauncher(common, trackBank, host);
        sceneMode = new PadSceneLauncher(common, this, sceneBank, host);

        // Callbacks de sélection de mode via Hardware
        hardware.getButton(0).pressedAction().addBinding(host.createAction(() -> {
            setActiveMode(clipMode, "Clip Mode (tracks 1 to 4)");
            trackBank.scrollPosition().set(0);
        }, () -> "Select Clip Mode tracks 1 to 4"));

        hardware.getButton(1).pressedAction().addBinding(host.createAction(() -> {
            setActiveMode(clipMode, "Clip Mode (tracks 5 to 8)");
            trackBank.scrollPosition().set(4);
        }, () -> "Select Clip Mode tracks 5 to 8"));

        hardware.getButton(2).pressedAction().addBinding(host.createAction(() -> {
            setActiveMode(clipMode, "Clip Mode (tracks 9 to 12)");
            trackBank.scrollPosition().set(8);
        }, () -> "Select Clip Mode tracks 9 to 12"));

        hardware.getButton(3).pressedAction().addBinding(host.createAction(() -> {
            setActiveMode(clipMode, "Clip Mode (tracks 13 to 16)");
            trackBank.scrollPosition().set(12);
        }, () -> "Select Clip Mode tracks 13 to 16"));

        hardware.getButton(4).pressedAction().addBinding(host.createAction(() -> {
            setActiveMode(sceneMode, "Scene Mode (scenes 1 to 64)");
        }, () -> "Select Scene Mode scenes 1 to 64"));

        hardware.getButton(5).pressedAction().addBinding(host.createAction(() -> {
            setActiveMode(instrumentMode, "Instrument Mode (pads are actives)");
        }, () -> "Select Instrument Mode 'pads are actives'"));
    }

    private void initObservers() {
        // Scroll
        trackBank.scrollPosition().addValueObserver(pos -> {
            activeMode.init(); // On laisse le mode gérer son propre rafraîchissement
        });

        // Couleur de piste
        cursorTrack.color().addValueObserver((r, g, b) -> {
            activeMode.cursorTrackColorObs((int)(r*255), (int)(g*255), (int)(b*255));
        });

        // Boucle des slots (Clips)
        for (int t = 0; t < 4; t++) {
            Track track = trackBank.getItemAt(t);
            track.subscribe(); // Indispensable pour garder la piste active
            //trackBank.getItemAt(t).clipLauncherSlotBank().setIndication(true);
            ClipLauncherSlotBank clips = track.clipLauncherSlotBank();
            for (int s = 0; s < clips.getSizeOfBank(); s++) {
                final int tt = t; final int ss = s;
                ClipLauncherSlot slot = clips.getItemAt(s);

                // 1. DIS AUX SLOTS QUE TU VEUX LES INFOS
                slot.color().markInterested();
                slot.hasContent().markInterested();
                slot.isRecording().markInterested();
                slot.isPlaying().markInterested();

                // 2. ENVOIE L'INFO AU MODE ACTIF SANS CONDITION
                // C'est le polymorphisme qui bosse pour toi ici.
                // 1. Pour la présence de contenu
                slot.hasContent().addValueObserver(has -> {
                    common.getClipSlots()[tt][ss].hasContent = has; // CRUCIAL : On met à jour le cache[cite: 5]
                    activeMode.clipContentObs(tt, ss, has);
                });

                // 2. Pour la couleur du clip
                slot.color().addValueObserver((r, g, b) -> {
                    PadColor pc = fr.akai.common.PadColorMapper.fromBitwigRGB((int)(r*255), (int)(g*255), (int)(b*255));
                    common.getClipSlots()[tt][ss].color = pc; // CRUCIAL : On stocke la couleur convertie[cite: 5, 7]
                    activeMode.clipColorObs(tt, ss, pc);
                });

                slot.hasContent().addValueObserver(has -> {
                    common.getClipSlots()[tt][ss].hasContent = has; // Mise à jour du cache indispensable
                    activeMode.clipContentObs(tt, ss, has);
                });
                slot.isRecording().addValueObserver(rec -> {
                    common.getClipSlots()[tt][ss].recording = rec;
                    activeMode.clipRecordObs(tt, ss, rec);
                });
                slot.isPlaying().addValueObserver(play -> {
                    common.getClipSlots()[tt][ss].playing = play;
                    activeMode.clipPlayingObs(tt, ss, play);
                });
            }
        }

        // Nom de l'instrument
        cursorDevice.name().markInterested();
        cursorDevice.name().addValueObserver(name -> {
            // Ici on garde le cast car seul PadInstrument possède cette méthode spécifique
            if (activeMode instanceof PadInstrument) {
                ((PadInstrument)activeMode).cursorTrackInstrumentNameObs(name);
            }
        });
    }

    public void setActiveMode(PadMode mode, String description) {
        if (activeMode == mode) {
            // Optionnel : on peut quand même forcer la notif si on change de banque
            host.showPopupNotification("Mode: " + description);
        }
        activeMode = mode;

        // 1. BASCULE DU MIDI (Le point manquant)
        // On active les notes pour l'instrument, on les coupe pour les clips/scènes
        setPadNotesEnabled(mode instanceof PadInstrument);

        // 2. INITIALISATION DES LEDS
        activeMode.init();

        // 3. LIAISONS PHYSIQUES (Simplifié grâce à PadMode)
        // Plus besoin de "instanceof" car initBindings est dans la classe mère
        activeMode.initBindings(hardware);

        // 4. NOTIFICATION
        host.showPopupNotification("Mode: " + description);
    }

    private void setPadNotesEnabled(boolean enabled) {
        if (padNoteInput == null) return; // Sécurité si pas encore initialisé

        if (enabled) {
            // MODE INSTRUMENT : On active les notes (Table 1:1)
            Integer[] table = new Integer[128];
            for (int i = 0; i < 128; i++) table[i] = i;
            padNoteInput.setKeyTranslationTable(table);
            padNoteInput.setShouldConsumeEvents(true);
        } else {
            // MODE CLIP/SCENE : On coupe les notes (Table vide/null)
            // Cela force Bitwig à envoyer le MIDI aux HardwareButtons
            Integer[] silentTable = new Integer[128];
            java.util.Arrays.fill(silentTable, -1);
            padNoteInput.setKeyTranslationTable(silentTable);
            padNoteInput.setShouldConsumeEvents(false);
        }
    }
    private void handleMidi(int status, int data1, int data2) {
        int type = status & 0xF0;
    }

    private void onSysex(String data) {
        switch (data) {
            case "f07f7f0605f7": transport.rewind(); break;
            case "f07f7f0604f7": transport.fastForward(); break;
            case "f07f7f0601f7": transport.stop(); break;
            case "f07f7f0602f7": transport.play(); break;
            case "f07f7f0606f7": transport.record(); break;
        }
    }

    @Override public void exit() { host.showPopupNotification("MPK249 Disconnected"); }
    @Override public void flush() {}

    public CursorTrack getCursorTrack() { return cursorTrack; }
    public CursorDevice getCursorDevice() { return cursorDevice; }
    public SettableBooleanValue getFollowCursorSetting() { return followCursorSetting; }
    public TrackBank getTrackBank() { return trackBank; }
    public MidiIn getMidiIn() { return midiIn; }
    public PadMode getActiveMode() {
        return activeMode;
    }
}