package com.dpforge.doom.wad;

public enum WadType {
    /**
     * The acronym IWAD is generally interpreted as "Internal WAD" and refers to a WAD file which contains
     * all of the game data for a complete game.
     */
    IWAD,
    /**
     * A PWAD, short for patch wad, is a WAD containing lumps of data created by a user as an add-on.
     * PWAD lumps are given priority over IWAD lumps in order for PWADs to replace anything that was included
     * in the original game.
     */
    PWAD
}
