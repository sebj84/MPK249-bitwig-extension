package fr.akai.mode;

import fr.akai.common.MPK2Common;
import fr.akai.common.MPK2Common.PadColor;
import fr.akai.common.MPK2Common.ClipBank;
import fr.akai.common.MPK2Common.ClipSlotData;
import fr.akai.common.MPK2Hardware;

import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.ClipLauncherSlotBank;
import com.bitwig.extension.controller.api.HardwareActionBindable;

public class PadClipLauncher extends PadMode {
    private final TrackBank trackBank;
    // Cache pour les actions de lancement (évite de les recréer sans arrêt)
    private final HardwareActionBindable[][] launchActions = new HardwareActionBindable[4][16];

    public PadClipLauncher(MPK2Common common, TrackBank trackBank, ControllerHost host) {
        super(common, host);
        this.trackBank = trackBank;

        // --- Pré-chargement des actions (Logique Hardware API) ---
        for (int t = 0; t < 4; t++) {
            ClipLauncherSlotBank slots = trackBank.getItemAt(t).clipLauncherSlotBank();
            for (int s = 0; s < 16; s++) {
                launchActions[t][s] = slots.getItemAt(s).launchAction();
            }
        }
    }

    @Override
    public void init() {
        // Comme dans ton ancien code : rafraîchissement total
        updateAllClipLEDs();
    }

    @Override
    public void initBindings(MPK2Hardware hardware) {
        // Nettoyage impératif pour éviter les conflits avec le mode Instrument
        clearAllPadBindings(hardware);

        // Application de ta logique de mapping (Tracks 0-3, Clips 0-15 sur Banques A-D)
        for (int bankIdx = 0; bankIdx < 4; bankIdx++) {
            ClipBank bank = ClipBank.values()[bankIdx];
            for (int t = 0; t < 4; t++) {
                for (int s = 0; s < 4; s++) {
                    int globalSlot = s + (bankIdx * 4); // Ton calcul clipOffset
                    int padIndex = getPadFromTrackSlot(t, globalSlot, bank);

                    if (padIndex != -1) {
                        hardware.getPad(padIndex).pressedAction().setBinding(launchActions[t][globalSlot]);
                    }
                }
            }
        }
    }

    // --- OBSERVATEURS (Reprise exacte de ton ancienne logique de mise à jour) ---

    @Override
    public void clipContentObs(int track, int slot, boolean hasContent) {
        if (!isValidClipIndex(track, slot)) return;
        super.clipContentObs(track, slot, hasContent);
        updateClipLED(track, slot);
    }

    @Override
    public void clipRecordObs(int track, int slot, boolean isRecording) {
        if (!isValidClipIndex(track, slot)) return;
        super.clipRecordObs(track, slot, isRecording);
        updateClipLED(track, slot);
    }

    @Override
    public void clipColorObs(int track, int slot, PadColor color) {
        // TEST ULTIME : On bypass TOUT, même le super et le cache
        if (!isValidClipIndex(track, slot)) return;
        super.clipColorObs(track, slot, color);
        updateClipLED(track, slot);
    }

    @Override
    public void clipPlayingObs(int track, int slot, boolean isPlaying) {
        if (!isValidClipIndex(track, slot)) return;
        super.clipPlayingObs(track, slot, isPlaying);
        updateClipLED(track, slot);
    }

    // --- LOGIQUE DE CALCUL (Ton code d'origine qui marchait d'enfer) ---

    private int getPadFromTrackSlot(int track, int slot, ClipBank bank) {
        if (track < 0 || track > 3) return -1;

        int clipOffset = bank.getSceneOffset() / 4;
        int localClip = slot - clipOffset;
        if (localClip < 0 || localClip > 3) return -1;

        int mirroredRow = 3 - track;
        int localPad = mirroredRow * 4 + localClip;
        return (bank.ordinal() * 16) + localPad;
    }

    private void updateClipLED(int track, int slot) {
        for (ClipBank bank : ClipBank.values()) {
            int pad = getPadFromTrackSlot(track, slot, bank);
            if (pad >= 0 && pad < 64) {
                applyClipLED(track, slot, pad);
            }
        }
    }

    private void applyClipLED(int track, int slot, int pad) {
        ClipSlotData clipData = common.getClipSlots()[track][slot];
        PadColor color;

        if (clipData.recording) color = PadColor.Red;
        else if (clipData.playing) color = PadColor.Green;
        else if (clipData.hasContent) color = clipData.color;
        else color = PadColor.Off;

        lightPad(pad, color);
    }

    private void updateAllClipLEDs() {
        // Suppression du clearAllPads pour éviter le scintillement, applyClipLED s'en charge
        for (int t = 0; t < 4; t++) {
            for (int s = 0; s < 16; s++) {
                updateClipLED(t, s);
            }
        }
    }
}