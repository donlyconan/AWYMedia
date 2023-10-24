package com.utc.donlyconan.media.app.utils;

import android.util.Pair;

import java.util.Stack;

public class Searches {

    public static int[][] evaluate(char []root, char []target, int start) {
        int rootLength = root.length;
        int targetLength = target.length;
        int matrix[][] = new int[rootLength][targetLength];

        // compare row.index with root.length - target.length
        for (int rowIndex = 0; rowIndex < rootLength - targetLength; rowIndex++) {

            // compare each char in the root with another in the target
            for (int rootIndex = rowIndex, targetIndex = 0; rootIndex < rootLength && targetIndex < targetLength;) {
                char ch = target[targetIndex];
                if(ch == root[rootIndex]) {
                    matrix[rowIndex][targetIndex] = 1;
                }
                targetIndex++;
                rootIndex++;
            }
        }
        return matrix;
    }


    public static int[][] evaluate2(char []root, char []target) {
        int rootLength = root.length;
        int targetLength = target.length;
        int matrix[][] = new int[rootLength][targetLength];

        // compare row.index with root.length - target.length
        for (int rowIndex = 0; rowIndex < rootLength - targetLength; rowIndex++) {

            // compare each char in the root with another in the target
            for (int rootIndex = rowIndex, targetIndex = 0; rootIndex < rootLength && targetIndex < targetLength;) {
                char ch = target[targetIndex];
                if(ch == root[rootIndex]) {
                    matrix[rowIndex][targetIndex] = 1;
                }
                targetIndex++;
                rootIndex++;
            }
        }
        return matrix;
    }

}


