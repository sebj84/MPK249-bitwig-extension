package fr.akai.common;

import com.bitwig.extension.controller.api.ControllerHost;

public class MPK2Common
{
    // ------------------------------------------------------------
    // Pad colors (MPK249 palette)
    // ------------------------------------------------------------
    public enum PadColor
    {
        Off(0x00),
        Red(0x01),
        Orange(0x02),
        Amber(0x03),
        Yellow(0x04),
        Green(0x05),
        Green_Blue(0x06),
        Aqua(0x07),
        Light_Blue(0x08),
        Blue(0x09),
        Purple(0x0A),
        Pink(0x0B),
        Hot_Pink(0x0C),
        Pastel_Purple(0x0D),
        Pastel_Green(0x0E),
        Pastel_Pink(0x0F),
        Grey(0x10);

        public final int value;
        PadColor(int v) { this.value = v; }
    }

    // ------------------------------------------------------------
    // AKAI SysEx constants
    // ------------------------------------------------------------
    public static final int PRODUCT_ID = 0x24;
    public static final int MIN_PAD_ON  = 1468;
    public static final int MIN_PAD_OFF = 1404;

    // ------------------------------------------------------------
    // Clip banks
    // ------------------------------------------------------------
    public enum ClipBank
    {
        BANK_A(0),
        BANK_B(16),
        BANK_C(32),
        BANK_D(48);

        private final int sceneOffset;
        ClipBank(int offset) { this.sceneOffset = offset; }
        public int getSceneOffset() { return sceneOffset; }
    }
    private ControllerHost host;

    // ------------------------------------------------------------
    // Clip slot data
    // ------------------------------------------------------------
    public static class ClipSlotData
    {
        public boolean hasContent = false;
        public boolean playing = false;
        public boolean recording = false;
        public PadColor color = PadColor.Off;
    }

    private final ClipSlotData[][] clipSlots = new ClipSlotData[8][16];

    public ClipSlotData[][] getClipSlots()
    {
        return clipSlots;
    }

    // ------------------------------------------------------------
    // Track color
    // ------------------------------------------------------------
    private PadColor currentTrackColor = PadColor.Off;

    public PadColor getCurrentTrackColor()
    {
        return currentTrackColor;
    }

    public void setCurrentTrackColor(PadColor c)
    {
        currentTrackColor = c;
    }

    // ------------------------------------------------------------
    // Pad MIDI tables
    // ------------------------------------------------------------
    private final int[] padMidiTableOn  = new int[128];
    private final int[] padMidiTableOff = new int[128];

    public MPK2Common()
    {
        for (int i = 0; i < 128; i++)
        {
            padMidiTableOn[i] = i;
            padMidiTableOff[i] = i;
        }

        for (int t = 0; t < 8; t++)
        {
            for (int s = 0; s < 16; s++)
            {
                clipSlots[t][s] = new ClipSlotData();
            }
        }
    }

    public void attachHost(ControllerHost host)
    {
        this.host = host;
    }

    // ------------------------------------------------------------
    // SysEx helpers
    // ------------------------------------------------------------
    public static int getMSB(int value)
    {
        return (value >> 7) & 0x7F;
    }

    public static int getLSB(int value)
    {
        return value & 0x7F;
    }

    // ------------------------------------------------------------
    // Getters / setters
    // ------------------------------------------------------------
    public int[] getPadMidiTableOn()
    {
        return padMidiTableOn;
    }

    // ------------------------------------------------------------
    // Pad LED control (SysEx AKAI MPK2)
    // ------------------------------------------------------------
    public void lightPad(PadColor color, int padNumber, boolean on)
    {
        if (host == null)
            return;

        int base = on ? MIN_PAD_ON : MIN_PAD_OFF;
        int padIdent = base + padNumber;

        String sysex = String.format(
            "F0 47 00 %02X 31 00 04 01 %02X %02X %02X F7",
            PRODUCT_ID & 0x7F,
            getMSB(padIdent),
            getLSB(padIdent),
            color.value & 0x7F
        );

        host.getMidiOutPort(1).sendSysex(sysex);
    }

    public void lightAllPads(PadColor color, boolean on)
    {
        if (host == null)
            return;

        int base = on ? MIN_PAD_ON : MIN_PAD_OFF;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
            "F0 47 00 %02X 31 00 43 40 %02X %02X",
            PRODUCT_ID & 0x7F,
            getMSB(base),
            getLSB(base)
        ));

        for (int i = 0; i < 64; i++)
        {
            sb.append(String.format(" %02X", color.value & 0x7F));
        }

        sb.append(" F7");

        host.getMidiOutPort(1).sendSysex(sb.toString());
    }
}
