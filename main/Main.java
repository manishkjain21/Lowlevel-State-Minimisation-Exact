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

    public static void main(String[] args) {

        if (args.length > 0) {
            System.out.println(" Current working directory : " + System.getProperty("user.dir"));

            String input_file_name = args[0];

            Parser p = new Parser();
            p.parseFile(input_file_name);

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
            //Formatting the given states as a Table below

            for (State s : state_in) {
                //System.out.print(s.getName()+" ");
                List<Long> x = s.getInputs();
                int len = x.size();
                for (int j = 0; j < len; j++) {
                    long input = x.get(j);
                    System.out.format("%d ", input);
                    System.out.print(s.getName() + " ");
                    State intermediate = s.getNextState(input);
                    System.out.print(intermediate.getName());
                    System.out.print(" ");
                    System.out.print(s.output(input));
                    System.out.println(" ");
                }

                System.out.println(" ");

            }
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

            int x = 100;  // arbitray value to validate for improper read
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
                                Toutputs[t] = outputs[s];
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
                            Toutputs[t] = outputs[s];
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
            System.out.println("The Moore Machine is as below: ");
            for (s = 0; s < total_len; s++) {
                System.out.format("%d ", Tinput_data[s]);
                System.out.print(TcurrentStates[s] + " ");
                System.out.print(TNextStates[s] + " ");
                System.out.format("%d\n", Toutputs[s]);
            }
            System.out.format("Original Transitions= %d \n", total_len - var_count);
            System.out.format("New States= %d\n", x);
            System.out.format("StateCounter= %d\n", state_counter);
            System.out.format("Variable Count= %d \n", var_count);
            System.out.format("%d ", total_len);
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
            // Remove the redundant states from the state transition table
            for (s = 0; s < total_len; s++) {
                for (j = s; j < total_len; j++) {
                    if (s != j) {
                        if ((input_col.get(s) == input_col.get(j)) && current_col.get(s).equals(current_col.get(j)) && next_col.get(s).equals(next_col.get(j))) {
                            //remove the state from list and decrement total length
                            input_col.remove(j);
                            current_col.remove(j);
                            next_col.remove(j);
                            output_col.remove(j);
                            total_len--;
                        }
                    }
                }
            }
            // Code Works properly upto this point
            //Check for same current state and next state in the transition table

            long resultant_val;
            for (s = 0; s < total_len; s++) {
                for (j = s; j < total_len; j++) {
                    if (s != j) {
                        if (current_col.get(s).equals(current_col.get(j)) && next_col.get(s).equals(next_col.get(j))) {
                            resultant_val = compare_inputs(input_col.get(s), input_col.get(j));

                            if (resultant_val != 0) {
                                input_col.set(s, resultant_val);
                                input_col.remove(j);
                                current_col.remove(j);
                                next_col.remove(j);
                                output_col.remove(j);
                                total_len--;

                            }
                        }
                    }
                }
            }

            for (s = 0; s < total_len; s++) {
                for (j = s; j < total_len; j++) {
                    if (s != j) {
                        if ((input_col.get(s) == input_col.get(j)) && (next_col.get(s).equals(next_col.get(j)))) {
                            //Comma was removed from here
                            current_col.set(s, current_col.get(s) + current_col.get(j));
                            input_col.remove(j);
                            current_col.remove(j);
                            next_col.remove(j);
                            output_col.remove(j);
                            total_len--;
                        }
                    }
                }
            }
            List<String> new_statenames = new ArrayList<String>();
            k = 0;
            int new_flag = 0;
            for (s = 0; s < total_len; s++) {
                for (j = 0; j < total_len; j++) {

                    if (current_col.get(s).contains(next_col.get(j))) {
                        next_col.set(j, current_col.get(s));
                        if (new_statenames.isEmpty()) {
                            new_statenames.add(current_col.get(s));
                            k++;
                        } else {
                            for (i = 0; i < k; i++) {
                                if (current_col.get(s).equals(new_statenames.get(i))) {
                                    new_flag = 0;
                                    break;
                                } else {
                                    new_flag = 1;
                                }
                            }
                            if (new_flag == 1) {
                                new_statenames.add(current_col.get(s));
                                k++;
                                new_flag = 0;
                            }
                        }

                    }
                }
            }
            for (s = 0; s < total_len; s++) {
                System.out.format("%d ", input_col.get(s));
                System.out.print(current_col.get(s) + " ");
                System.out.print(next_col.get(s) + " ");
                System.out.format("%d\n", output_col.get(s));
            }
            System.out.format("%d ", total_len);

            BufferedWriter bw;
            String filename = "Minimised_FSM.blif";

            try {
                bw = new BufferedWriter(new FileWriter(filename));
                bw.write(".model fsm\n");
                bw.write(".start_kiss\n");
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
                bw.write(".end_kiss");
                bw.flush();
                bw.close();
            } catch (IOException e) {
                System.out.println("An IOException occured");
            }

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
