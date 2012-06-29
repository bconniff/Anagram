/* Copyright (c) 2012, Brendan Conniff
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of Brendan Conniff nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package anagram.gui;

import anagram.dict.WordSet;
import anagram.util.ScrabbleComparator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;
import java.util.*;

public class AnagramGui extends JFrame {
   private final ScrabbleComparator com = new ScrabbleComparator();
   private final String buttonText = "Find Anagrams";
   private final JButton solveButton = new JButton(buttonText);
   private final DictionaryMenu dict;

   public AnagramGui() {
      super("Anagram Solver");

      dict = new DictionaryMenu(this);

      final JTextField lettersField = new LetterField();
      final JCheckBox useAll = new JCheckBox("Use All Letters");
      final JCheckBox scrabbleMode = new JCheckBox("Scrabble Mode");
      final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 0, 1);
      final JSpinner lengthSpinner = new JSpinner(model);
      final JTextArea output = new JTextArea();
      final JLabel lettersLabel = new JLabel("Available Letters");
      final JLabel lengthLabel = new JLabel("Word Length");
      final JScrollPane scrollOutput = new JScrollPane(output);
      final Border b = new EmptyBorder(4, 4, 4, 4);
      final JPanel input = new JPanel();
      final JMenuBar menu = new JMenuBar();

      menu.add(dict);
      setJMenuBar(menu);

      input.setLayout(new GridLayout(0,2));
      input.add(lettersLabel);
      input.add(lettersField);
      input.add(lengthLabel);
      input.add(lengthSpinner);
      input.add(scrabbleMode);
      input.add(useAll);

      setLayout(new BorderLayout());
      add(input, BorderLayout.NORTH);
      add(scrollOutput, BorderLayout.CENTER);
      add(solveButton, BorderLayout.SOUTH);

      input.setBorder(b);

      output.setBorder(b);
      output.setEditable(false);

      lettersField.setHorizontalAlignment(JTextField.RIGHT);
      lettersField.getDocument().addDocumentListener(new DocumentListener() {
         private void update() {
            final int max = lettersField.getText().length();
            final int val = model.getNumber().intValue();
            model.setMaximum(max);
            model.setValue((useAll.isSelected() || val > max) ? max : val);
         }

         public void changedUpdate(DocumentEvent e) { update(); }
         public void removeUpdate(DocumentEvent e) { update(); }
         public void insertUpdate(DocumentEvent e) { update(); }
      });

      solveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final String txt = lettersField.getText();
            final int len = model.getNumber().intValue();
            final boolean scrabble = scrabbleMode.isSelected();

            lock();

            new Thread(new Runnable() {
               public void run() {
                  String result = "";

                  try {
                     final String[] answers = scrabble
                        ? dict.getWords().scrabble(txt, len)
                        : dict.getWords().lookup(txt, len);

                     if (scrabble)
                        Arrays.sort(answers, com);

                     boolean first = true;

                     for (String s: answers) {
                        if (first)
                            first = false;
                        else
                            result += "\n";
                        result += s;
                        if (scrabble)
                           result += " (" + com.getScore(s) + ")";
                     }
                  } catch (OutOfMemoryError e) {
                     e.printStackTrace();
                     SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           dispose();
                           GuiUtils.error(
                              "Ran out of memory trying to find anagrams.\n" +
                              "The application will exit...");
                           exit();
                        }
                     });
                     return;
                  } catch (Exception e) {
                     e.printStackTrace();
                     SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                           GuiUtils.error(
                              "Failed while trying to find anagrams.\n" +
                              "This is probably a bug...");
                        }
                     });
                  }

                  final String r = result;

                  SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                        output.setText(r);
                        output.setCaretPosition(0);
                        unlock();
                     }
                  });
               }
            }).start();
         }
      });

      useAll.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (useAll.isSelected()) {
               lengthSpinner.setValue(lettersField.getText().length());
               lengthSpinner.setEnabled(false);
            } else {
               lengthSpinner.setEnabled(true);
            }
         }
      });

      pack();
      final Dimension d = getSize();
      setMinimumSize(new Dimension(d.width,d.height*2));
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }

   public void unlock() {
      solveButton.setText(buttonText);
      solveButton.setEnabled(true);
      dict.setEnabled(true);
   }

   public void lock() {
      dict.setEnabled(false);
      solveButton.setEnabled(false);
      solveButton.setText("Loading...");
   }

   public void addDictionary(String file, String name) {
      dict.addDictionary(file, name);
   }

   public void exit() {
      this.processWindowEvent(
         new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
   }
}
