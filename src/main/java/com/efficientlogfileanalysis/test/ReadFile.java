package com.efficientlogfileanalysis.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Scanner;

public class ReadFile {

    public static void readScanner(String path) throws IOException
    {
//        Scanner scanner = new Scanner(new BufferedReader(new FileReader(path)));
        Scanner scanner = new Scanner(FileChannel.open(Paths.get(path)));
//        Scanner scanner = new Scanner(new File(path));
        scanner.useDelimiter("\n");
        String line;
        while (scanner.hasNext()) {
            line = scanner.next();
        }
    }

    public static void readBufferedReader(String path) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String line;
        while((line = br.readLine()) != null);
    }

    public static String readLine(BufferedReader br) throws IOException
    {
        int s1 = 0;
        StringBuilder formattedString = new StringBuilder("");
        while ((s1 = br.read()) != -1) {
            char character = (char) s1;

            formattedString.append(character);

            if (character == '\n')
                return formattedString.toString();
        }
        String result = formattedString.toString();
        return result.isEmpty() ? null : result;
    }

    public static void readBufferedReaderOwn(String path) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String line;
        while((line = readLine(br)) != null);
    }

    public static void readWithBuffer(String path) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String line;
        char[] buffer = new char[10000];
        int lineLength = 0;
        int sizeRead = 0;

        while(sizeRead != -1)
        {
            sizeRead += br.read(buffer, sizeRead, buffer.length - 1 - sizeRead);

            for(; lineLength < sizeRead; lineLength++){
                if(buffer[lineLength] == '\n'){
                    line = new String(buffer, 0, lineLength + 1);

                    sizeRead -= lineLength;
                    lineLength = 0;
                    break;
                }
            }
        }

    }

    public static void main(String[] args) {

    }

}
