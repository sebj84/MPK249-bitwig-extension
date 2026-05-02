package fr.akai.common;

import fr.akai.common.MPK2Common.PadColor;

public class PadColorMapper
{
    // Bitwig official colors (converted to 0–255) and mapped to adapt to the 16 colors of the pads of MPK249/261
    private static final int[][] BITWIG = {
        {84, 84, 84},     // Dark Grey
        {122, 122, 122},  // Light Grey
        {201, 201, 201},  // White
        {134, 137, 172},  // Purple Grey
        {163, 121, 67},   // Dark Brown
        {198, 159, 112},  // Light Brown
        {87, 97, 198},    // Purple Blue
        {132, 138, 224},  // Light Purple Blue
        {149, 73, 203},   // Purple
        {217, 56, 113},   // Pink
        {217, 46, 36},    // Red
        {255, 87, 6},     // Orange
        {217, 157, 16},   // Gold
        {115, 152, 20},   // Lime
        {0, 157, 71},     // Green
        {0, 166, 148},    // Aqua
        {0, 153, 217},    // Sky Blue
        {188, 118, 240},  // Light Purple
        {225, 102, 145},  // Light Pink
        {236, 97, 87},    // Pink Orange
        {255, 131, 62},   // Light Orange
        {228, 183, 78},   // Light Gold
        {160, 192, 76},   // Light Lime
        {62, 187, 98},    // Light Green
        {67, 210, 185},   // Light Aqua
        {68, 200, 255}    // Light Sky Blue
    };

    private static final PadColor[] MAP = {
        PadColor.Grey,          // Dark Grey
        PadColor.Grey,          // Light Grey
        PadColor.Grey,          // White
        PadColor.Purple,        // Purple Grey
        PadColor.Orange,        // Dark Brown
        PadColor.Orange,        // Light Brown
        PadColor.Light_Blue,    // Purple Blue
        PadColor.Pastel_Purple, // Light Purple Blue
        PadColor.Purple,        // Purple
        PadColor.Pink,          // Pink
        PadColor.Red,           // Red
        PadColor.Orange,        // Orange
        PadColor.Orange,        // Gold
        PadColor.Green_Blue,    // Lime
        PadColor.Pastel_Green,  // Green
        PadColor.Aqua,          // Aqua
        PadColor.Aqua,          // Sky Blue
        PadColor.Pastel_Purple, // Light Purple
        PadColor.Hot_Pink,      // Light Pink
        PadColor.Pastel_Pink,   // Pink Orange
        PadColor.Orange,        // Light Orange
        PadColor.Orange,        // Light Gold
        PadColor.Pastel_Green,  // Light Lime
        PadColor.Pastel_Green,  // Light Green
        PadColor.Aqua,          // Light Aqua
        PadColor.Light_Blue     // Light Sky Blue
    };

    public static PadColor fromBitwigRGB(int r, int g, int b)
    {
    	// if dark color or clip is off we switch off all the led under the akai PAD.
        if (r < 90 && g < 90 && b < 90) {
            return PadColor.Off;
        }
        //distance between the real color in bitwig versus the 16 colors of the Akai PAD
    	int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < BITWIG.length; i++)
        {
            int dr = BITWIG[i][0] - r;
            int dg = BITWIG[i][1] - g;
            int db = BITWIG[i][2] - b;

            int dist = dr*dr + dg*dg + db*db;

            if (dist < bestDistance)
            {
                bestDistance = dist;
                bestIndex = i;
            }
        }

        return MAP[bestIndex]; //return the best color regarding the clip / track selected
    }
}
