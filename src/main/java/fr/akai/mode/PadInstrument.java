package fr.akai.mode;

import fr.akai.common.MPK2Common;
import fr.akai.common.MPK2Common.PadColor;
import fr.akai.common.PadColorMapper;
import fr.akai.common.MPK2Hardware;

import com.bitwig.extension.controller.api.ControllerHost;

public class PadInstrument extends PadMode
{

    private final boolean[] drumKeys = new boolean[128];
    private boolean usingDrumRack = false;

    public PadInstrument(
            MPK2Common common,
            ControllerHost host)
    {
        // On ne passe plus padInput ici, car géré globalement par l'extension
        super(common, host);
    }

    @Override
    public void init()
    {
        PadColor color = common.getCurrentTrackColor();
        if (color == null)
            color = PadColor.Blue;

        // Rafraîchissement initial des LEDs
        if (usingDrumRack)
            lightPadsForDrumRack(color);
        else
            lightAllPads(color);
    }

    @Override
    public void initBindings(MPK2Hardware hardware)
    {
        // NETTOYAGE CRUCIAL : Supprime les liens vers les clips/scènes
        // pour laisser les notes MIDI "couler" vers le NoteInput
        clearAllPadBindings(hardware);
    }

    @Override
    public void cursorTrackColorObs(int r, int g, int b)
    {
        PadColor mapped = PadColorMapper.fromBitwigRGB(r, g, b);
        common.setCurrentTrackColor(mapped);

        // On ne met à jour les LEDs que si l'instrument est le mode actif
        updateDisplay();
    }

    @Override
    public void cursorTrackInstrumentNameObs(String name)
    {
        // Détection du mode Drum Rack selon l'instrument sélectionné
        usingDrumRack = "DrumMachine".equals(name);

        if (usingDrumRack)
            java.util.Arrays.fill(drumKeys, false);

        updateDisplay();
    }

    @Override
    public void cursorTrackpitchObs(int key, String name)
    {
        // On enregistre quelles notes du Drum Rack contiennent des sons
        if (key >= 0 && key < 128)
        {
            drumKeys[key] = true;
            if (usingDrumRack)
                updateDisplay();
        }
    }

    /**
     * Centralise la mise à jour des LEDs pour éviter les redondances
     */
    private void updateDisplay()
    {
        PadColor color = common.getCurrentTrackColor();
        if (usingDrumRack)
            lightPadsForDrumRack(color);
        else
            lightAllPads(color);
    }

    private void lightPadsForDrumRack(PadColor color)
    {
        for (int pad = 0; pad < 64; pad++)
        {
            // Mapping standard des pads Bitwig (Note 36 = C1)[cite: 8]
            int note = pad + 36;

            if (drumKeys[note])
                lightPad(pad, color);
            else
                lightPad(pad, PadColor.Off);
        }
    }
}