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

package anagram.dict;

import java.util.*;
import java.io.*;

public class WordSet {
   private final Trie root = new Trie();

   public WordSet(InputStream is) throws IOException {
      final InputStreamReader isr = new InputStreamReader(is);
      final BufferedReader r = new BufferedReader(isr);

      for (String s = r.readLine(); s != null; s = r.readLine())
         root.add(s);

      r.close();
      isr.close();
      is.close();
   }

   private void fill
      ( Set<String> r,
        Trie t,
        StringBuilder w,
        int len,
        boolean scr )
   {
      if (t.isAccept() && (len == 0 || (scr && len > 0))) {
         r.add(t.getWord());
      } else {
         final int wlen = w.length();
         for (int i = 0; i < wlen; i++) {
            final Trie ch = t.getChild(w.charAt(i));
            if (ch != null) {
               final char c = w.charAt(i);
               w.deleteCharAt(i);
               fill(r, ch, w, len - 1, scr);
               w.insert(i, c);
            }
         }
      }
   }

   private Set<String> lookupSet(String w, int len, boolean scr) {
      Set<String> result = new HashSet<String>();
      fill(result, root, new StringBuilder(w), len, scr);
      return result;
   }

   public String[] lookup(String word, int len) {
      return lookupSet(word, len, false).toArray(new String[0]);
   }

   public String[] lookup(String word) {
      return lookup(word, word.length());
   }

   public String[] scrabble(String word, int len) {
      return lookupSet(word, len, true).toArray(new String[0]);
   }

   public String[] scrabble(String word) {
      return scrabble(word, word.length());
   }
}
