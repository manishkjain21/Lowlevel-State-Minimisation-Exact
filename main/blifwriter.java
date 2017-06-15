/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.*;
import java.util.*;
import lowlevel.*;

public class blifwriter {

    final static String lineSeparator = System.getProperty("line.separator");
    ParsedFile fsm_mini;
    BufferedWriter bw;
    String name;
    Map<String, String> state_map = new HashMap<String, String>();
    List<String> states_name = new ArrayList<String>();

    public void writeblif(ParsedFile fsm, String filename) {

        fsm_mini = fsm;

        name = filename.substring(filename.lastIndexOf(File.separatorChar) + 1, filename.lastIndexOf('.'));
        try {
            System.out.println(filename);
            bw = new BufferedWriter(new FileWriter(filename));

            writeHeader();
            writecomments();
            writetransitionsection();
            writeoutputsection();

            bw.flush();
            bw.close();

        } catch (IOException e) {

        }
    }

    private void writeHeader() throws IOException {

        /*write .model*/
        bw.write(".model " + name + "_state_minimise" + lineSeparator);
        /*write .inputs*/
        bw.write(".inputs");
        for (int i = fsm_mini.getNumInputs() - 1; i >= 0; i--) {
            bw.write(" I" + Integer.toString(i));
        }
        bw.write(lineSeparator);

        /*write .outputs*/
        bw.write(".outputs");
        for (int i = fsm_mini.getNumOutputs() - 1; i >= 0; i--) {
            bw.write(" O" + Integer.toString(i));
        }
        bw.write(lineSeparator);

        /*write .m*/
        bw.write(".m ");
        bw.write(Integer.toString(fsm_mini.getNum_states()) + lineSeparator);
        bw.write(".clock clk" + lineSeparator + lineSeparator);
    }

    private void writecomments() throws IOException {

        State[] states = fsm_mini.getStates();
        int i = 0;
        for (State s : states) {
            state_map.put(s.getName(), longtostr((1 << i), fsm_mini.getNum_states()));
            states_name.add(s.getName());
            bw.write("# " + s.getName() + " " + longtostr((1 << i), fsm_mini.getNum_states()) + lineSeparator);
            i++;
        }

        bw.write(lineSeparator);

    }

    private void writetransitionsection() throws IOException {

        bw.write("### transition section begin ###" + lineSeparator);

        /*write .inputs*/
        //{
        for (int i = fsm_mini.getNum_states() - 1; i >= 0; i--) {

            bw.write(lineSeparator);
            bw.write(".names");
            for (int j = fsm_mini.getNum_states() - 1; j >= 0; j--) {
                bw.write(" S" + Integer.toString(j));
            }

            for (int k = fsm_mini.getNumInputs() - 1; k >= 0; k--) {
                bw.write(" I" + Integer.toString(k));
            }

            bw.write(" next_S" + Integer.toString(i));
            bw.write(lineSeparator);

            State[] states = fsm_mini.getStates();

            for (State s : states) {

                String next_state = states_name.get(i);

                List<Long> input_data = s.getInputs();
                int len = input_data.size();
                for (int j = 0; j < len; j++) {

                    if (next_state.equals(s.getNextState(input_data.get(j)).getName())) {
                        // wirite code for string to long
                        String stringname = state_map.get(s.getName());
                        bw.write(stringname + changetostring(input_data.get(j), fsm_mini.getNumInputs()) + " 1" + lineSeparator);
                    }

                }

            }

        }

        bw.write(lineSeparator);

        for (int i = fsm_mini.getNum_states() - 1; i >= 0; i--) {
            bw.write(".latch next_S" + Integer.toString(i) + " S" + Integer.toString(i) + " re clk 0" + lineSeparator);
        }

        bw.write(lineSeparator);
        bw.write("### transition section ending ###" + lineSeparator + lineSeparator);

    }

    private void writeoutputsection() throws IOException {
        bw.write("### output section begin ###" + lineSeparator);

        for (int i = fsm_mini.getNumOutputs() - 1; i >= 0; i--) {
            bw.write(lineSeparator);
            bw.write(".names");
            for (int j = fsm_mini.getNum_states() - 1; j >= 0; j--) {
                bw.write(" S" + Integer.toString(j));
            }

            for (int k = fsm_mini.getNumInputs() - 1; k >= 0; k--) {
                bw.write(" I" + Integer.toString(k));
            }

            bw.write(" next_O" + Integer.toString(i));
            bw.write(lineSeparator);
            State[] states = fsm_mini.getStates();
            for (State s : states) {
                long[][] outputs = s.getOutputs();
                for (long[] output1 : outputs) {
                    long input = output1[1];
                    long output = output1[2];
                    String out = Helper.longToOutputString(output);
                    boolean check = (out.charAt(i) == '1');
                    if (check) {
                        String stringname = state_map.get(s.getName());
                        bw.write(stringname + changetostring(input, fsm_mini.getNumInputs()) + " 1" + lineSeparator);
                    }
                }

            }

        }

        bw.write(lineSeparator);

        for (int i = fsm_mini.getNumOutputs() - 1; i >= 0; i--) {
            bw.write(".latch next_O" + Integer.toString(i) + " O" + Integer.toString(i) + " re clk 0" + lineSeparator);
        }

        bw.write(lineSeparator);
        bw.write("### output section ending ###");

    }

    public static String longtostr(long input, int num) {
        String str = "";
        int loop_var = 0;
        loop_var = num - 1;
        while (loop_var >= 0) {
            if (((input >> loop_var) & 1) == 1) {
                str = str.concat("1");
            } else {
                str = str.concat("0");
            }
            loop_var--;
        }

        return str;
    }

    public static String changetostring(long x, int r) {
        long temp_res;
        String m = "";
        int loop_var = r;
        while (loop_var > 0) {
            temp_res = (x >> 2 * (loop_var - 1)) & 3;
            switch ((int) temp_res) {
                case 1:
                    m = m.concat("0");
                    break;
                case 2:
                    m = m.concat("1");
                    break;
                case 3:
                    m = m.concat("-");
                    break;
                default:
                    break;
            }
            loop_var--;
        }

        return m;
    }

}
