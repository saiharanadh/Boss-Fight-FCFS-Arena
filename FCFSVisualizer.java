import java.awt.*;
import java.util.*;
import javax.swing.*;

class Process {
    String name;
    int at, bt, ct, tat, wt, rt;
    Color color;

    Process(String name, int at, int bt, Color color) {
        this.name = name;
        this.at = at;
        this.bt = bt;
        this.color = color;
    }
}

public class FCFSVisualizer extends JFrame {
    JPanel readyPanel, cpuPanel, donePanel;
    JButton startBtn;
    java.util.List<Process> processes;
    JLabel[] processLabels;
    JLabel timeLabel; // Current time counter

    public FCFSVisualizer() {
        setTitle("FCFS CPU SCHEDULING ANIMATION");
        setSize(850, 500);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ===== Current Time Counter =====
        timeLabel = new JLabel("Current Time: 0", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timeLabel.setBounds(300, 10, 250, 30);
        add(timeLabel);

        // ===== Title =====
        JLabel title = new JLabel("BOSS FIGHT ARENA GAME", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(200, 40, 450, 40);
        add(title);

        // ===== Ready Queue =====
        JLabel readyLbl = new JLabel("PLAYERS IN LOBBY");
        readyLbl.setBounds(60, 90, 150, 20);
        add(readyLbl);

        readyPanel = new JPanel();
        readyPanel.setBounds(40, 120, 180, 250);
        readyPanel.setBackground(new Color(220, 220, 220));
        readyPanel.setLayout(new GridLayout(4, 1, 5, 5));
        add(readyPanel);

        // ===== CPU Box =====
        JLabel cpuLbl = new JLabel("BOSS FIGHT");
        cpuLbl.setBounds(400, 90, 100, 20);
        add(cpuLbl);

        cpuPanel = new JPanel();
        cpuPanel.setBounds(350, 120, 150, 150);
        cpuPanel.setBackground(new Color(180, 240, 255));
        cpuPanel.setLayout(new BorderLayout());
        add(cpuPanel);

        // ===== Completed Queue =====
        JLabel doneLbl = new JLabel("PLAYERS WON");
        doneLbl.setBounds(650, 90, 150, 20);
        add(doneLbl);

        donePanel = new JPanel();
        donePanel.setBounds(630, 120, 180, 250);
        donePanel.setBackground(new Color(220, 220, 220));
        donePanel.setLayout(new GridLayout(4, 1, 5, 5));
        add(donePanel);

        // ===== Start Button =====
        startBtn = new JButton("Start Fight");
        startBtn.setBounds(340, 380, 170, 30);
        add(startBtn);

        // ===== Process Data =====
        processes = new ArrayList<>();
        processes.add(new Process("Knight_Peter", 0, 3, Color.PINK));
        processes.add(new Process("Archer_John", 1, 5, Color.ORANGE));
        processes.add(new Process("Warrior_Mike", 2, 2, Color.GREEN));
        processes.add(new Process("Fighter_Tom", 3, 4, Color.CYAN));

        // Sort by arrival time (important)
        processes.sort(Comparator.comparingInt(p -> p.at));

        // Create labels for each process
        processLabels = new JLabel[processes.size()];
        for (int i = 0; i < processes.size(); i++) {
            JLabel lbl = new JLabel(processes.get(i).name, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(processes.get(i).color);
            lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            readyPanel.add(lbl);
            processLabels[i] = lbl;
        }

        // Button Action
        startBtn.addActionListener(e -> startSimulation());
        setVisible(true);
    }

    void startSimulation() {
        startBtn.setEnabled(false);

        // Backend FCFS Logic (Calculations)
        int time = 0;
        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            if (p.at > time) {
                time = p.at; // CPU idle until process arrives
            }
            p.rt = time - p.at;
            time += p.bt;
            p.ct = time;
            p.tat = p.ct - p.at;
            p.wt = p.tat - p.bt;
        }

        // Start animation thread
        new Thread(this::animateProcesses).start();
    }

    void animateProcesses() {
        try {
            int currentTime = 0;

            for (int i = 0; i < processes.size(); i++) {
                Process p = processes.get(i);
                JLabel lbl = processLabels[i];

                // Wait until process arrival
                if (currentTime < p.at) {
                    currentTime = p.at;
                    updateTime(currentTime);
                    Thread.sleep(500);
                }

                // Move from Ready to CPU
                readyPanel.remove(lbl);
                readyPanel.revalidate();
                readyPanel.repaint();

                JLabel moveLabel = new JLabel(p.name, SwingConstants.CENTER);
                moveLabel.setOpaque(true);
                moveLabel.setBackground(p.color);
                moveLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                moveLabel.setBounds(100, 200, 60, 30);
                add(moveLabel);
                repaint();

                int x = 100;
                while (x < 380) { // animate moving to CPU
                    x += 5;
                    moveLabel.setLocation(x, 200);
                    Thread.sleep(30);
                }

                // Inside CPU (simulate execution)
                cpuPanel.add(moveLabel);
                cpuPanel.revalidate();
                cpuPanel.repaint();

                // Update time during execution to match CT
                int startTime = currentTime;
                int endTime = p.ct;
                while (currentTime < endTime) {
                    Thread.sleep(800); // 1 unit = 0.8s
                    currentTime++;
                    updateTime(currentTime);
                }

                // Move to Completed
                cpuPanel.remove(moveLabel);
                cpuPanel.revalidate();
                cpuPanel.repaint();
                donePanel.add(moveLabel);
                donePanel.revalidate();
                donePanel.repaint();

                Thread.sleep(500); // small delay between processes
            }

            showResults();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateTime(int t) {
        SwingUtilities.invokeLater(() -> timeLabel.setText("TIME INSIDE CPU: " + t));
    }

    void showResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("Player\tAT\tBT\tCT\tTAT\tWT\tRT\n");
        double avgTat = 0, avgWt = 0;

        for (Process p : processes) {
            sb.append(p.name + "\t" + p.at + "\t" + p.bt + "\t" + p.ct + "\t" + p.tat + "\t" + p.wt + "\t" + p.rt + "\n");
            avgTat += p.tat;
            avgWt += p.wt;
        }

        avgTat /= processes.size();
        avgWt /= processes.size();
        sb.append("\nAverage TAT: " + String.format("%.2f", avgTat));
        sb.append("\nAverage WT: " + String.format("%.2f", avgWt));

        JOptionPane.showMessageDialog(this,
                new JTextArea(sb.toString()),
                "FCFS Scheduling Results",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FCFSVisualizer::new);
    }
}
