package fr.akai.mode;

import fr.akai.common.MPK2Common;
import fr.akai.common.MPK2Common.PadColor;
import fr.akai.common.MPK2Hardware;
import com.bitwig.extension.controller.api.ControllerHost;

/**
 * Classe mère abstraite gérant la logique commune à tous les modes de pads.
 */
public abstract class PadMode {
    protected final MPK2Common common;
    protected final ControllerHost host;

    public PadMode(MPK2Common common, ControllerHost host) {
        this.common = common;
        this.host = host;
    }

    // --- MÉTHODES OBLIGATOIRES (À implémenter dans chaque sous-classe) ---

    /** Initialisation visuelle (LEDs) du mode */
    public abstract void init();

    /** Configuration des liens physiques (Bindings) du mode */
    public abstract void initBindings(MPK2Hardware hardware);

    // --- LE "KARCHER" (Nettoyage de sécurité) ---

    /**
     * Supprime tous les liens entre les pads et les actions Bitwig.
     * Indispensable pour éviter que le mode Instrument ne déclenche des clips.
     */
    public void clearAllPadBindings(MPK2Hardware hardware) {
        for (int i = 0; i < 64; i++) {
            hardware.getPad(i).pressedAction().clearBindings();
            hardware.getPad(i).releasedAction().clearBindings();
        }
    }

    // --- OBSERVATEURS DE PISTE ET INSTRUMENT (Signatures pour @Override) ---

    public void cursorTrackColorObs(int r, int g, int b) {}

    public void cursorTrackInstrumentNameObs(String name) {}

    public void cursorTrackpitchObs(int key, String name) {}

    // --- OBSERVATEURS DE CLIPS (Signatures pour @Override) ---
    // Ces méthodes permettent à l'extension de compiler même si le mode actif
    // n'est pas le ClipLauncher.

    public void clipContentObs(int track, int slot, boolean hasContent) {}

    public void clipRecordObs(int track, int slot, boolean isRecording) {}

    public void clipPlayingObs(int track, int slot, boolean isPlaying) {}

    public void clipColorObs(int track, int slot, PadColor color) {}

    // --- UTILITAIRES LEDS ---

    protected void lightPad(int padIndex, PadColor color) {
        common.lightPad(color, padIndex, false);
    }

    protected void lightAllPads(PadColor color) {
        common.lightAllPads(color, false);
    }

    /** Helper pour valider les coordonnées de clips */
    protected boolean isValidClipIndex(int track, int slot) {
        return track >= 0 && track < 4 && slot >= 0 && slot < 16;
    }
}