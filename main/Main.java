package main;

import io.Parser;
import java.util.Arrays;
import java.util.List;
import lowlevel.ParsedFile;
import lowlevel.State;
import io.DotWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import moore_machine.java;
/**
 * Main class
 *
 * @author Wolf & Gottschling
 *
 */
public class Main {

    public static int x = 0;

    public static void main(String[] args) {

        if (args.length > 0) {
            long startTime = System.currentTimeMillis();

            System.out.println(" Current working directory : " + System.getProperty("user.dir"));

            String input_file_name = args[0];

            Parser p = new Parser();
            p.parseFile(input_file_name);

            //Remove the extension from file name
            input_file_name = input_file_name.substring(0, input_file_name.indexOf("."));
            // Representation of the FSM
            ParsedFile fsm = p.getParsedFile();

            // TODO - here you go 
            State[] state_in = fsm.getStates();

            int init_states, init_transitions, init_input, init_outputs;
//                        System.out.println(fsm);
            init_states = fsm.getNum_states();
            init_transitions = fsm.getNum_transitions();
            init_outputs = fsm.getNumOutputs();
            init_input = fsm.getNumInputs();

            if (init_transitions == 0) {
                init_transitions = 255;

            }
            System.out.format("I=%d S=%d T=%d O=%d\n", init_input, init_states, init_transitions, init_outputs);

            //Comparing all same states for different outputs for conversion to Moore Machine
            // Appending the new state to the system and Increasing the State Count    
            // while appending new states check for any duplicate state by traversing through the pairs array
            // if there are duplicates change the corresponding states to new state
            // The below code checks for same state and marks them                        
            String[] NextStates;
            NextStates = new String[init_transitions];
            String[] currentStates;
            currentStates = new String[init_transitions];
            long[] outputs;
            outputs = new long[init_transitions];
            long[] input_data = new long[init_transitions];
            int k = 0, j = 0, i = 0;
            int total_writes = 0;
            for (State s : state_in) {
                List<Long> x = s.getInputs();
                int len = x.size();
                for (j = 0; j < len; j++) {
                    currentStates[total_writes] = s.getName();
                    input_data[total_writes] = x.get(j);
                    NextStates[total_writes] = s.getNextState(input_data[total_writes]).getName();
                    outputs[total_writes] = s.output(input_data[total_writes]);
                    total_writes++;
                }
            }

            k = 0;
            int state_counter = 0;
            int temp = 0;
            moore_machine[] st = new moore_machine[init_states];
            for (temp = 0; temp < init_states; temp++) {
                st[temp] = new moore_machine();
            }
            for (State s : state_in) {
                String statename = s.getName();

                List<Long> out = new ArrayList<Long>();
                List<String> new_string = new ArrayList<String>();
                int new_flag = 0;
                k = 0;
                for (j = 0; j < total_writes; j++) {

                    if (statename.equals(NextStates[j])) {

                        if (out.isEmpty()) {
                            out.add(outputs[j]);
                            k++;
                        } else {
                            for (i = 0; i < k; i++) {
                                if (outputs[j] == out.get(i)) {
                                    new_flag = 0;
                                    break;
                                } else {
                                    new_flag = 1;
                                }
                            }
                            if (new_flag == 1) {
                                out.add(outputs[j]);
                                k++;
                                new_flag = 0;
                            }
                        }
                    }
                }

                long[] outl = new long[k];
                String[] newstring = new String[k];
                for (j = 0; j < k; j++) {
                    newstring[j] = new_statestring(statename, j);
                    try {
                        outl[j] = out.get(j);
                    } catch (Exception e) {
                    }
                }
                try {
                    st[state_counter].initialise_moore(k, newstring, outl, statename);
                } catch (Exception e) {

                }
                state_counter++;
            }

            x = 100;  // arbitray value to validate for improper read
            String sname;
            int loop_var = 0;

            int var_count = 0;
            for (loop_var = 0; loop_var < state_counter; loop_var++) {

                x = st[loop_var].getcount();

                if (x > 1) //for states with more than one outputs
                {
                    sname = st[loop_var].getname();
                    for (int s = 0; s < total_writes; s++) {
                        if (sname.equals(NextStates[s])) {
                            for (i = 0; i < x; i++) {
                                if (outputs[s] == st[loop_var].output[i]) {
                                    NextStates[s] = st[loop_var].new_state[i];
                                }
                            }
                        }
                        if (sname.equals(currentStates[s])) {
                            var_count++;
                        }
                    }
                }
            }
            int total_len;
            if (init_transitions == 255) {
                total_len = var_count + total_writes;
            } else {
                total_len = var_count + init_transitions;
            }

            String[] TNextStates;
            TNextStates = new String[total_len];
            String[] TcurrentStates;
            TcurrentStates = new String[total_len];
            long[] Toutputs;
            Toutputs = new long[total_len];
            long[] Tinput_data = new long[total_len];
            String current_name;
            int s = 0, t = 0;
            for (loop_var = 0; loop_var < state_counter; loop_var++) {
                current_name = st[loop_var].statename;
                x = st[loop_var].getcount();

                if (x > 1) //for states with more than one outputs
                {
                    for (; s < total_writes; s++) {
                        if (current_name.equals(currentStates[s])) {
                            for (i = 0; i < x && t < total_len; i++) {
                                TNextStates[t] = NextStates[s];
                                TcurrentStates[t] = st[loop_var].new_state[i];
                                Tinput_data[t] = input_data[s];
                                Toutputs[t] = st[loop_var].output[i];
                                t++;
                            }
                        } else {

                            break;

                        }
                    }

                } else {
                    for (; s < total_writes && t < total_len; s++) {
                        if (current_name.equals(currentStates[s])) {
                            TNextStates[t] = NextStates[s];
                            TcurrentStates[t] = currentStates[s];
                            Tinput_data[t] = input_data[s];
                            Toutputs[t] = st[loop_var].output[0];
                            t++;
                        } else {
                            break;
                        }
                    }
                }
            }
            // Calculate the new number of states
            x = 0;
            for (loop_var = 0; loop_var < state_counter; loop_var++) {
                s = st[loop_var].getcount();
                x += s;
            }
            System.out.println("The Normal Moore Machine is as below: ");
            for (s = 0; s < total_len; s++) {
                System.out.format("%d ", Tinput_data[s]);
                System.out.print(TcurrentStates[s] + " ");
                System.out.print(TNextStates[s] + " ");
                System.out.format("%d\n", Toutputs[s]);
            }
            System.out.format("Original Transitions= %d \n", total_len - var_count);
            System.out.format("New States= %d\n", x);
            System.out.format("Original States= %d\n", state_counter);
            System.out.format("Transitions Added= %d \n", var_count);
            System.out.format("New Transitions= %d \n", total_len);
            List<Long> input_col = new ArrayList<Long>();
            List<String> current_col = new ArrayList<String>();
            List<String> next_col = new ArrayList<String>();
            List<Long> output_col = new ArrayList<Long>();
            //Copy the tranisition table in a List
            for (s = 0; s < total_len; s++) {
                input_col.add(Tinput_data[s]);
                current_col.add(TcurrentStates[s]);
                next_col.add(TNextStates[s]);
                output_col.add(Toutputs[s]);
            }
            //The below code checks for total number of states in the input table 
            List<String> new_statename = new ArrayList<String>();
            Map<String, Long> state_num = new HashMap<String, Long>();
            k = 0;
            x = 0;
            int flag = 0;
            for (s = 0; s < total_len; s++) {
                String statename = current_col.get(s);
                x = 0;
                for (j = 0; j < total_len; j++) {

                    if (new_statename.isEmpty()) {
                        new_statename.add(current_col.get(s));
                        state_num.put(statename, output_col.get(s));
                        k++;

                    } else {
                        for (i = 0; i < k; i++) {
                            if (current_col.get(s).equals(new_statename.get(i))) {
                                flag = 0;
                                break;
                            } else {
                                flag = 1;
                            }
                        }
                        if (flag == 1) {
                            new_statename.add(current_col.get(s));
                            state_num.put(statename, output_col.get(s));
                            k++;
                            flag = 0;
                        }
                    }
                }
            }

            // The below Code writes a kiss2 file in the directory whcih represents the Moore Representation of the given state machine
            String filename = "Moore_" + input_file_name + ".kiss2";
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(filename));

                bw.write(".i " + init_input + "\n");
                bw.write(".o " + init_outputs + "\n");
                bw.write(".s " + k + "\n");
                bw.write(".p " + total_len + "\n");

                for (s = 0; s < total_len; s++) {
                    bw.write(changetostring(input_col.get(s), init_input) + " ");
                    bw.write(current_col.get(s) + " ");
                    bw.write(next_col.get(s) + " ");
                    bw.write(changetostring(output_col.get(s), init_outputs) + "\n");
                }
                bw.flush();
                bw.close();
            } catch (IOException e) {
                System.out.println("An IOException occured");
            }

            Parser p2 = new Parser();
            p2.parseFile(filename);
            ParsedFile fsm2 = p2.getParsedFile();

            // The below code iniialises the states of moore machine with their inputs
            State[] state_in2 = fsm2.getStates();
            //Set the initial block partition to 1, implying all the states are in the same block
            // partition them sequentially based on the outputs
            for (State s_loop : state_in2) {
                s_loop.setCode(0);
            }
            for (int u = 0; u < fsm2.getNum_states(); u++) {
                System.out.print("The state is: ");
                System.out.print(state_in2[u].getName());
                System.out.format(" %d\n", state_in2[u].getCode());
            }
            // Write the inital Block Partition based on inputs
            x = 0;
            for (s = 0; s < fsm2.getNum_states(); s++) {
                String state_n = state_in2[s].getName();
                long out1 = state_num.get(state_n);
                if (state_in2[s].getCode() == 0) {
                    x++;
                    state_in2[s].setCode(x);
                    for (j = s + 1; j < fsm2.getNum_states(); j++) {
                        String state_n1 = state_in2[j].getName();
                        long out2 = state_num.get(state_n1);
                        if (out1 == out2 && s != j && (state_in2[j].getCode() == 0)) {
                            state_in2[j].setCode(x);
                        } else {

                        }
                    }
                } else {

                }

            }
            for (int u = 0; u < fsm2.getNum_states(); u++) {
                System.out.print("The state is: ");
                System.out.print(state_in2[u].getName());
                System.out.format(" %d\n", state_in2[u].getCode());
            }

            for (s = 0; s < fsm2.getNum_states(); s++) {

                state_in2 = partition_state(state_in2, fsm2);
                for (int u = 0; u < fsm2.getNum_states(); u++) {
                    System.out.print("The state is: ");
                    System.out.print(state_in2[u].getName());
                    System.out.format(" %d\n", state_in2[u].getCode());
                }
            }

//            int write_flag = 0;
//            for (s = 0; s < fsm2.getNum_states(); s++) {
//                for (j = s + 1; j < fsm2.getNum_states(); j++) {
//                    String state_n1 = state_in2[s].getName();
//                    String state_n2 = state_in2[j].getName();
//                    if (state_in2[s].getCode() == state_in2[j].getCode()) {
//                        List<Long> input1 = state_in2[s].getInputs();
//                        List<Long> input2 = state_in2[j].getInputs();
//                        for (int in1 = 0; in1 < input1.size(); in1++) {
//                            for (int in2 = 0; in2 < input2.size(); in2++) {
//                                if (input1.get(in1) == input2.get(in2)) {
//                                    if (state_in2[s].getNextState(input1.get(in1)).getCode() == state_in2[j].getNextState(input2.get(in2)).getCode()) {
//                                        continue;
//                                    } else {
//                                        write_flag = 1;
//                                    }
//                                }
//                                if (x == fsm2.getNum_states() || (write_flag == 1)) {
//                                    break;
//                                }
//                            }
//                            if (x == fsm2.getNum_states() || (write_flag == 1)) {
//                                break;
//                            }
//                        }
//                        
//                    }
//                    if (write_flag == 1) {
//                            x++;
//                            state_in2[j].setCode(x);
//                            
//                            write_flag = 0;
//
//                        }
//
//                    if (x == fsm2.getNum_states()) {
//                        break;
//                    }
//
//                }
//                for (int u = 0; u < fsm2.getNum_states(); u++) {
//                    System.out.print("The state is: ");
//                    System.out.print(state_in2[u].getName());
//                    System.out.format(" %d\n", state_in2[u].getCode());
//                }
//                if (x == fsm2.getNum_states()) {
//                    break;
//                }
//
//            }
            for (s = 0; s < fsm2.getNum_states(); s++) {
                System.out.print("The state is: ");
                System.out.print(state_in2[s].getName());
                System.out.format("Partition Block = %d\n", state_in2[s].getCode());
            }
            // Code Works properly upto this point
            // Code for Minimisation using Equivalence Logic
            System.out.format("Minimised Transitions = %d \n", total_len);
            System.out.format("Minimised States = %d ", k);
            long endTime = System.currentTimeMillis();

            System.out.println("Execution Time is " + (endTime - startTime) + " milliseconds");

            filename = "Minimise1_FSM" + input_file_name + ".kiss2";

            try {
                bw = new BufferedWriter(new FileWriter(filename));

                bw.write(".i " + init_input + "\n");
                bw.write(".o " + init_outputs + "\n");
                bw.write(".s " + k + "\n");
                bw.write(".p " + total_len + "\n");

                for (s = 0; s < total_len; s++) {
                    bw.write(changetostring(input_col.get(s), init_input) + " ");
                    bw.write(current_col.get(s) + " ");
                    bw.write(next_col.get(s) + " ");
                    bw.write(changetostring(output_col.get(s), init_outputs) + "\n");
                }
                bw.flush();
                bw.close();
            } catch (IOException e) {
                System.out.println("An IOException occured");
            }

            String filename1 = "test_1.blif";
            blifwriter blif = new blifwriter();
            blif.writeblif(fsm, filename1);

            Parser p1 = new Parser();
            p1.parseFile(filename);
            ParsedFile fsm1 = p1.getParsedFile();
            filename = "test_2.blif";
            blifwriter blif2 = new blifwriter();
            blif2.writeblif(fsm1, filename);

            filename = "minimised_FSM.dot";
            try {
                bw = new BufferedWriter(new FileWriter(filename));
                bw.write("digraph " + "Minimised_FSM" + " {\n");
                for (s = 0; s < total_len; s++) {
                    bw.write("\t " + current_col.get(s) + " -> " + next_col.get(s) + "[label=\"" + changetostring(input_col.get(s), init_input) + "\"" + ",weight=\"" + changetostring(input_col.get(s), init_input) + "\"];\n");

                }
                bw.write("}");
                bw.flush();
                bw.close();
            } catch (IOException e) {
                System.out.println("An IOException occured");
            }

        } else {
            System.out.println("No input argument given");
        }
    }

    public static String next_state(String x, int val) {
        String next_states, prefix;
        int len = x.length();
        int j;
        long y;
        for (j = 0; j < len; j++) {
            if (x.charAt(j) >= '0' && x.charAt(j) <= '9') {
                break;
            }
        }
        next_states = x.substring(j);

        y = Long.parseLong(next_states);
        y = y + 1;
        next_states = String.valueOf(y);
        if (j == 0) {
            prefix = null;
        } else {
            prefix = x.substring(0, j);
            next_states = prefix.concat(next_states);
        }

        return next_states;
    }

// //sample Test Code for below function        
//                        int i=0;
//                        String str = "st2";
//                        int val = 2;
//                        System.out.println(new_statestring(str, val));
    public static String new_statestring(String x, int val) {
        String new_str;
        char append = 'a';
        append = (char) (append + val);
        new_str = x + append;
        return new_str;
    }

    public void tabulate_states(ParsedFile fsm) {

        State[] state_in = fsm.getStates();
        int init_states, init_transitions, init_input, init_outputs;
        init_states = fsm.getNum_states();
        init_transitions = fsm.getNum_transitions();
        init_outputs = fsm.getNumOutputs();
        init_input = fsm.getNumInputs();
        System.out.format("I=%d S=%d T=%d O=%d\n", init_input, init_states, init_transitions, init_outputs);

        //Formatting the given states as a Table below
        for (State s : state_in) {
            //System.out.print(s.getName()+" ");
            List<Long> x = s.getInputs();
            int len = x.size();
            for (int j = 0; j < len; j++) {
                long input = x.get(j);
                System.out.format("%8d ", input);
                System.out.print(s.getName() + " ");
                State intermediate = s.getNextState(input);
                System.out.print(intermediate.getName());
                System.out.print(" ");
                System.out.print(s.output(input));
                System.out.println(" ");
            }

            System.out.println(" ");

        }

    }

    public static long compare_inputs(long x1, long x2) {
        long temp = 0, loop_var = 0, result = 0, temp_res;
        int count_flag = 0;
        temp = x1 ^ x2;
        while (loop_var < Long.SIZE) {
            temp_res = (temp >> loop_var) & 3;
            switch ((int) temp_res) {
                case 0:
                    temp_res = (x1 >> loop_var) & 3;
                    break;
                case 1:
                    temp_res = 2;
                    break;
                case 2:
                    temp_res = 1;
                    break;

                case 3:
                    temp_res = 3;
                    count_flag++;
                    break;
                default:
                    break;
            }
            result |= (temp_res << loop_var);
            loop_var += 2;

        }
        if (count_flag > 1) {
            result = 0;
        }

        return result;
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

    public static int combinestates(List<Long> input, List<String> current_state, List<String> next_state, List<Long> output, int length, int s) {
        int i, j;

        for (j = s; j < length; j++) {
            if (s != j) {
                if ((input.get(s) == input.get(j)) && (next_state.get(s).equals(next_state.get(j))) && (output.get(s) == output.get(j))) {
                    //Comma was removed from here
                    if (current_state.get(s).equals(current_state.get(j))) {

                    } else {

                        current_state.set(s, current_state.get(s) + "," + current_state.get(j));
                        input.remove(j);
                        current_state.remove(j);
                        next_state.remove(j);
                        output.remove(j);
                        length--;

                        for (i = 0; i < length; i++) {
                            if ((current_state.get(s).contains(current_state.get(i))) && s != i) {
                                current_state.set(i, current_state.get(s));
                            }
                            if (current_state.get(s).contains(next_state.get(i))) {
                                next_state.set(i, current_state.get(s));
                            }

                        }
                        break;

                    }
                }
            }
        }

        return length;

    }

    //Check for same current state and next state in the transition table
    public static int combineinputs(List<Long> input, List<String> current_state, List<String> next_state, List<Long> output, int length) {
        long resultant_val;
        int s, j;
        for (s = 0; s < length; s++) {
            for (j = s; j < length; j++) {
                if (s != j) {
                    if (current_state.get(s).equals(current_state.get(j)) && next_state.get(s).equals(next_state.get(j)) && (output.get(s) == output.get(j))) {
                        resultant_val = compare_inputs(input.get(s), input.get(j));

                        if (resultant_val != 0) {
                            input.set(s, resultant_val);
                            input.remove(j);
                            current_state.remove(j);
                            next_state.remove(j);
                            output.remove(j);
                            length--;

                        }
                    }
                }
            }
        }

        return length;
    }

    public static int redundant_transitions(List<Long> input, List<String> current_state, List<String> next_state, List<Long> output, int length) {
        int s, j;
        for (s = 0; s < length; s++) {
            for (j = s; j < length; j++) {
                if (s != j) {
                    if ((input.get(s) == input.get(j)) && current_state.get(s).equals(current_state.get(j)) && next_state.get(s).equals(next_state.get(j)) && (output.get(s) == output.get(j))) {
                        //remove the state from list and decrement total length
                        input.remove(j);
                        current_state.remove(j);
                        next_state.remove(j);
                        output.remove(j);
                        length--;
                    }
                }
            }
        }
        return length;

    }

    public static State[] partition_state(State[] state_in2, ParsedFile fsm2) {
        int count=0, compare=0;
        int write_flag = 0;
        for (int s = 0; s < fsm2.getNum_states(); s++) {

            for (int j = 0; j < fsm2.getNum_states(); j++) {
                write_flag = 0;
                count=0;
                compare=0;
                String state_n1 = state_in2[s].getName();
                String state_n2 = state_in2[j].getName();
                if (s != j) {
                    List<Long> input1 = state_in2[s].getInputs();
                    List<Long> input2 = state_in2[j].getInputs();
                    for (int in1 = 0; in1 < input1.size(); in1++) 
                        for (int in2 = 0; in2 < input2.size(); in2++) 
                            if (input1.get(in1) == input2.get(in2)) {
                            count++;
                                    
                            }
                    for (int in1 = 0; in1 < input1.size(); in1++) {
                        for (int in2 = 0; in2 < input2.size(); in2++) {
                            if (input1.get(in1) == input2.get(in2)) {
                                if (state_in2[s].getNextState(input1.get(in1)).getCode() == state_in2[j].getNextState(input2.get(in2)).getCode()) {
                                    compare++;
                                    if(count==compare && (state_in2[s].output(input1.get(in1))==state_in2[j].output(input2.get(in2))) )
                                    {
                                        state_in2[j].setCode(state_in2[s].getCode());
                                    }
                                    continue;
                                } else {
                                    write_flag = 1;
                                }
                            }
                            if (x == fsm2.getNum_states() || (write_flag == 1)) {
                                break;
                            }
                        }
                        if (x == fsm2.getNum_states() || (write_flag == 1)) {
                            break;
                        }
                    }

                }
                if (write_flag == 1 && (state_in2[s].getCode() == state_in2[j].getCode())) {
                    x++;
                    state_in2[j].setCode(x);

                    write_flag = 0;
                    return state_in2;
                }

                if (x == fsm2.getNum_states()) {
                    return state_in2;
                }

            }

        }

        return state_in2;
    }

    //public static int
}

//                        String[] currentstates;
//                        currentstates = new String[init_states];
//                        for(State s:state_in)
//                        {
//                            currentstates[i]=s.getName();
//                            i++;
//                        }
//                        Arrays.sort(currentstates);
//                        
//                        String LastState=currentstates[init_states-1];
//                        System.out.println(LastState);
//The below code converts into next state string                          
//                        String st = state_in[0].getName();
//                        String st_n;
//                        st_n = next_state(st);
//                        System.out.println(st_n);
//                        //We iniitialise the pair variable considering the maximum matching output we can have for the program
//                        List<Integer> pair_1 = new ArrayList<Integer>();
//                        List<Integer> pair_2 = new ArrayList<Integer>();
//                       
//                        int total_pairs=0;
//                        for(j=0; j < i; j++){
//                            for(k = j; k < i; k++){
//                                if((NextStates[j].equals(NextStates[k]))&&(j!=k)&&(outputs[j]==outputs[k]))
//                                {   pair_1.add(j);
//                                    pair_2.add(k);
//                                    total_pairs++;
//                                }
//                            }
//                        }
//                        // necessary to convert back to Integer[]
//                        Integer[] pair1 = pair_1.toArray(new Integer[0]); 
//                        Integer[] pair2 = pair_2.toArray(new Integer[0]); 
//                        for(j=0;j<total_pairs;j++)
//                            System.out.format("%d %d\n",pair1[j], pair2[j]);
//                        for(loop_var=0; loop_var < state_counter; loop_var++){
//                            
//                            x = st[loop_var].getcount();
//                            
//                            if(x > 1)   //for states with more than one outputs
//                            {   System.out.println(x);
//                                for(int s=0; s < state_counter; s++){
//                                System.out.print(s);
//                                sname = st[loop_var].getname();
//                                System.out.println(sname);
//                                List<Long> xinput = state_in[s].getInputs();
//                                int len = xinput.size();
//                                for(j=0; j<len;j++){
//                                    long input = xinput.get(j);
//                                    
//                                    if(sname.equals(state_in[s].getNextState(input).getName())){
//                                        for( i=0 ; i<x ; i++){
//                                            if(state_in[s].output(input)==st[loop_var].output[i]){
//                                                state_in[s].getNextState(input).changestate(st[loop_var].new_state[i]);
//                                            }    
//                                        }
//                                    }
//                                }
//                                
//                                
//                                }
//                            }
//                            
//                            
//                        }
//                        for (State sta:state_in)
//                        {
//                            //System.out.print(s.getName()+" ");
//                            List<Long> x1 = sta.getInputs();
//                            int len = x1.size();
//                            for(j=0; j<len;j++){
//                                long input = x1.get(j);
//                                System.out.format("%8d ", input);
//                                System.out.print(sta.getName()+" ");
//                                State intermediate = sta.getNextState(input);
//                                System.out.print(intermediate.getName());
//                                System.out.print(" ");
//                                System.out.print(sta.output(input));
//                                System.out.println(" ");
//                            }
//                                
//                            System.out.println(" ");
//                            
//                        }
// The below code prints the current states in a tabular format                        
//                        for(int s=0; s < total_writes; s++)
//                        {   
//                            System.out.format("%d ", input_data[s]);
//                            System.out.print(currentStates[s]+" ");
//                            System.out.print(NextStates[s]+" ");
//                            System.out.format("%d\n", outputs[s]);
//                        }
// The below code writes states in .dot format
//                        String output_filename = "Minimised_state.dot";
//                        DotWriter out_write_help = new DotWriter();
//                        out_write_help.writeDot(fsm, output_filename);
//                        System.out.println("Output Written");
//                        System.out.println("Completed");
//Compare the 2 output and check for new states
//                        for (State s:state_in)
//                        {                            
//                            List<Long> x = s.getInputs();
//                            int len = x.size();
//                           
//                            for(j=0; j < i; j++){
//                                for(k = j; k < i; k++){
//                                    if((NextStates[j].equals(NextStates[k]))&&(i!=j)&&(outputs[j]==outputs[k]))
//                                    {   pair_1[l] = j;
//                                    pair_2[l] = k;
//                                    l++;
//                                    }
//                                }
//                            }
//                            
//                        }  
//            // The below code writes states in .dot format
//            String output_filename = "Minimised_state.dot";
//            DotWriter out_write_help = new DotWriter();
//            out_write_help.writeDot(fsm, output_filename);
//            System.out.println("Output Written");
//            System.out.println("Completed");
//put a loop herefor continuous checking
//            for (s = 0; s < total_len; s++) {
//                // check for redundancy
//                total_len = redundant_transitions(input_col, current_col, next_col, output_col, total_len);
//                //combine inputs
//                total_len = combineinputs(input_col, current_col, next_col, output_col, total_len);
//                //Combine states
//                total_len = combinestates(input_col, current_col, next_col, output_col, total_len, s);
//            }
//The below code checks for number of final states to count
//            for (s = 0; s < total_len; s++) {
//                System.out.format("%d ", input_col.get(s));
//                System.out.print(current_col.get(s) + " ");
//                System.out.print(next_col.get(s) + " ");
//                System.out.format("%d\n", output_col.get(s));
//            }
//Formatting the given states as a Table below
//
//            for (State s : state_in) {
//                //System.out.print(s.getName()+" ");
//                List<Long> x = s.getInputs();
//                int len = x.size();
//                for (int j = 0; j < len; j++) {
//                    long input = x.get(j);
//                    System.out.format("%d ", input);
//                    System.out.print(s.getName() + " ");
//                    State intermediate = s.getNextState(input);
//                    System.out.print(intermediate.getName());
//                    System.out.print(" ");
//                    System.out.print(s.output(input));
//                    System.out.println(" ");
//                }
//
//                System.out.println(" ");
//
//            }
