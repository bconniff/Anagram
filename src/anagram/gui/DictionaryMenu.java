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

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.*;

public class DictionaryMenu extends JMenu {
   private final ButtonGroup buttons = new ButtonGroup();
   private final AnagramGui parent;
   private boolean first = true;
   private WordSet words = null;
   private int idx = 0;
   private int prev = 0;

   public DictionaryMenu(AnagramGui parent, String name) {
      super(name);
      this.parent = parent;

      final JMenuItem r = new JMenuItem("Load File...");
      addSeparator();
      add(r);
      r.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final File f = GuiUtils.getFile();
            if (f != null) {
               addDictionary(f);
               ((JRadioButtonMenuItem)getItem(idx-1)).doClick();
            }
         }
      });
   }

   public DictionaryMenu(AnagramGui parent) { this(parent, "Dictionary"); }

   private void error(Throwable e, String msg, boolean exit) {
      final boolean x = exit;
      final String m = msg;
      e.printStackTrace();

      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            ((JRadioButtonMenuItem)getItem(prev)).setSelected(true);
            if (x) parent.dispose();
            GuiUtils.error(m);
            if (x) parent.exit();
         }
      });
   }

   private void error(Throwable e, String msg) { error(e, msg, false); }

   private void spawnWorker(InputStream is, int buttonIndex) {
      final int i = buttonIndex;
      final InputStream in = is;

      parent.lock();

      new Thread(new Runnable() {
         public void run() {
            try {
               words = new WordSet(in);
               prev = i;
            } catch (OutOfMemoryError e) {
               error(
                  e,
                  "Ran out of memory trying to open file.\n" +
                  "The application will exit...",
                  true);
               return;
            } catch (Exception e) {
               error(
                  e,
                  "Failed to read dictionary file.\n" +
                  "This is probably a bug...");
            }

            try {
               in.close();
            } catch (Exception e) {
               error(
                  e,
                  "Failed to close file.\n" +
                  "Not sure why that happened.");
            }
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  parent.unlock();
               }
            });
         }
      }).start();
   }

   public void addDictionary(String file, String name) {
      final String f = file;
      final String n = name;
      final int buttonIndex = idx++;

      final JRadioButtonMenuItem r = new JRadioButtonMenuItem(n);
      buttons.add(r);
      insert(r, buttonIndex);

      r.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            spawnWorker(getClass().getResourceAsStream(f), buttonIndex);
         }
      });

      if (first) {
         first = false;
         r.doClick();
      }
   }

   public void addDictionary(File file, String name) {
      final JRadioButtonMenuItem r = new JRadioButtonMenuItem(name);
      final File f = file;
      final int buttonIndex = idx++;

      buttons.add(r);
      insert(r, buttonIndex);

      r.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               InputStream is = new FileInputStream(f);
               spawnWorker(is, buttonIndex);
            } catch (IOException x) {
               x.printStackTrace();
               GuiUtils.error(
                  "Failed to open file.\n" +
                  "Perhaps it doesn't exist?");
            }
         }
      });

      if (first) {
         first = false;
         r.doClick();
      }
   }

   public void addDictionary(File f) { addDictionary(f, f.toString()); }
   public WordSet getWords() { return words; }
}
