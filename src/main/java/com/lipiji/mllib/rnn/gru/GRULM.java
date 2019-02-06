package com.lipiji.mllib.rnn.gru;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;

import com.lipiji.mllib.dataset.CharText;
import com.lipiji.mllib.layers.MatIniter;
import com.lipiji.mllib.layers.MatIniter.Type;
import com.lipiji.mllib.utils.LossFunction;

// Language Model using GRU
public class GRULM {
    private GRU gru;
    
    public GRULM(int inSize, int outSize, MatIniter initer) {
        gru = new GRU(inSize, outSize, initer);
    }
    
    private void train(CharText ctext, double lr) {
        Map<Integer, String> indexChar = ctext.getIndexChar();
        Map<String, DoubleMatrix> charVector = ctext.getCharVector();
        List<String> sequence = ctext.getSequence();
        for (int i = 0; i < 100; i++) {
            double error = 0;
            double num = 0;
            double start = System.currentTimeMillis();
            for (int s = 0; s < sequence.size(); s++) {
                String seq = sequence.get(s);
                if (seq.length() < 3) {
                    continue;
                }
                
                Map<String, DoubleMatrix> acts = new HashMap<>();
                // forward pass
                System.out.print(String.valueOf(seq.charAt(0)));
                for (int t = 0; t < seq.length() - 1; t++) {
                    DoubleMatrix xt = charVector.get(String.valueOf(seq.charAt(t)));
                    acts.put("x" + t, xt);

                    gru.active(t, acts);
                   
                    DoubleMatrix predcitYt = gru.decode(acts.get("h" + t));
                    acts.put("py" + t, predcitYt);
                    DoubleMatrix trueYt = charVector.get(String.valueOf(seq.charAt(t + 1)));
                    acts.put("y" + t, trueYt);
                    
                    System.out.print(indexChar.get(predcitYt.argmax()));
                    error += LossFunction.getMeanCategoricalCrossEntropy(predcitYt, trueYt);
                    
                }
                
                System.out.println();

                // bptt
                gru.bptt(acts, seq.length() - 2, lr);
                
                num +=  seq.length();
            }
            System.out.println("Iter = " + i + ", error = " + error / num + ", time = " + (System.currentTimeMillis() - start) / 1000 + "s");
        }
    }

    public static void main(String[] args) {
        int hiddenSize = 100;
        double lr = 0.5;
        CharText ct = new CharText();
        GRULM lstm = new GRULM(ct.getCharIndex().size(), hiddenSize, new MatIniter(Type.Uniform, 0.1, 0, 0));
        lstm.train(ct, lr);
    }

}
