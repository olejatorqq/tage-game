package javenue.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JOptionPane;
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;



public class Pyatnashki extends JFrame {
    private JPanel panel = new JPanel(new GridLayout(4, 4, 2, 2));
    private int[] stats = new int[2];
    private int[][] numbers = new int[4][4];

    public static void write (String filename, int[]x) throws IOException{
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        for (int i = 0; i < x.length; i++) {
            outputWriter.write(Integer.toString(x[i]));
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }

    public Pyatnashki() {
        super("Пятнашки");

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("stats.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int [] tall = new int [2];
        int i = 0;
        while(scanner.hasNextInt()){
            tall[i++] = scanner.nextInt();
        }
        stats = tall;

        setBounds(200, 200, 300, 300);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        createMenu();

        Container container = getContentPane();
        panel.setDoubleBuffered(true);
        container.add(panel);

        generate();
        repaintField(numbers);

    }

    private static void saveArrayToFileAny(int[][] arr, int max, String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            for (int i = 0; i < max; i++) {
                for (int j = 0; j < max; j++) {
                    bw.write(String.valueOf(arr[i][j]) + " ");
                }
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generate() {

        Random generator = new Random();
        int[] invariants = new int[16];

        do {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    numbers[i][j] = 0;
                    invariants[i * 4 + j] = 0;
                }
            }

            for (int i = 1; i < 16; i++) {
                int k, l;
                do {
                    k = generator.nextInt(4);
                    l = generator.nextInt(4);
                }
                while (numbers[k][l] != 0);
                numbers[k][l] = i;

                invariants[k * 4 + l] = i;
            }
        }

        while (!canBeSolved(invariants));

    }

    private boolean canBeSolved(int[] invariants) {
        int sum = 0;
        for (int i = 0; i < 16; i++) {
            if (invariants[i] == 0) {
                sum += i / 4;
                continue;
            }

            for (int j = i + 1; j < 16; j++) {
                if (invariants[j] < invariants[i])
                    sum ++;
            }
        }
        System.out.println(sum % 2 == 0);
        if (sum % 2 == 0){
            stats[0] += 1;
            try {
                write("stats.txt",stats);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return sum % 2 == 0;
    }

    public void repaintField(int massive[][]) {
        panel.removeAll();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                JButton button = new JButton(Integer.toString(massive[i][j]));
                button.setFocusable(false);
                panel.add(button);
                if (numbers[i][j] == 0) {
                    button.setVisible(false);
                } else
                    button.addActionListener(new ClickListener());
            }
        }

        panel.validate();
        panel.repaint();
    }

    public boolean checkWin() {
        boolean status = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 && j > 2)
                    break;
                if (numbers[i][j] != i * 4 + j + 1) {
                    status = false;
                }
            }
        }
        return status;
    }

    private void createMenu() {
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("Настройки");

        for (String fileItem : new String [] { "Новая игра", "Сохранение", "Загрузка", "Статистика", "Победа", "Выход" }) {
            JMenuItem item = new JMenuItem(fileItem);
            item.setActionCommand(fileItem.toLowerCase());
            item.addActionListener(new NewMenuListener());
            fileMenu.add(item);
        }
        fileMenu.insertSeparator(1);

        menu.add(fileMenu);
        setJMenuBar(menu);
    }

    private class NewMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if ("новая игра".equals(command)) {
                generate();
                repaintField(numbers);
            }
            if ("сохранение".equals(command)) {
                saveArrayToFileAny(numbers,4, "saves.txt");
            }
            if ("загрузка".equals(command)){
                String path = "saves.txt";
                File file = new File(path);
                if (file.length() == 0) {
                    System.out.println("Файл пуст");
                }
                else {
                    Scanner scn = null;
                    try {
                        scn = new Scanner(file);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    ArrayList<String[]> nums = new ArrayList<>();

                    while (scn.hasNext()) {
                        nums.add(scn.nextLine().split(" "));
                    }

                    int columns = nums.get(0).length;
                    int[][] arr = new int[nums.size()][columns];
                    Iterator<String[]> iter = nums.iterator();
                    for (int i = 0; i < arr.length; i++) {
                        String[] s = iter.next();
                        for (int j = 0; j < columns; j++) {
                            arr[i][j] = Integer.parseInt(s[j]);
                        }
                    }
                    scn.close();


                    numbers = arr;
                    repaintField(numbers);
                }


            }
            if ("статистика".equals(command)){
                String full;
                full= "Количисество побед: " + stats[1] + "\nКоличество игр: " + stats[0];
                JOptionPane.showMessageDialog(null, full);
            }
            if ("победа".equals(command)){
                numbers = new int[][]{{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};
                repaintField(numbers);
            }
            if ("выход".equals(command)) {
                System.exit(0);
            }
        }
    }

    private class ClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            button.setVisible(false);
            String name = button.getText();
            change(Integer.parseInt(name));
        }
    }

    public void change(int num) {
        int i = 0, j = 0;
        for (int k = 0; k < 4; k++) {
            for (int l = 0; l < 4; l++) {
                if (numbers[k][l] == num) {
                    i = k;
                    j = l;
                }
            }
        }
        if (i > 0) {
            if (numbers[i - 1][j] == 0) {
                numbers[i - 1][j] = num;
                numbers[i][j] = 0;
            }
        }
        if (i < 3) {
            if (numbers[i + 1][j] == 0) {
                numbers[i + 1][j] = num;
                numbers[i][j] = 0;
            }
        }
        if (j > 0) {
            if (numbers[i][j - 1] == 0) {
                numbers[i][j - 1] = num;
                numbers[i][j] = 0;
            }
        }
        if (j < 3) {
            if (numbers[i][j + 1] == 0) {
                numbers[i][j + 1] = num;
                numbers[i][j] = 0;
            }
        }

        repaintField(numbers);

        if (checkWin()) {
            stats[1] += 1;
            try {
                write("stats.txt",stats);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "YOU WIN!", "Congratulations", JOptionPane.INFORMATION_MESSAGE);
            generate();
            repaintField(numbers);
        }
    }

    public static void main(String[] args) {
        JFrame app = new Pyatnashki();

        app.setVisible(true);
    }
}
