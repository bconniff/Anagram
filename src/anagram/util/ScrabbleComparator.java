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

package anagram.util;

import java.util.*;

public class ScrabbleComparator implements Comparator<String> {
   private final Map<String,Integer> cache;

   public ScrabbleComparator() {
      cache = new HashMap<String,Integer>();
   }

   public int compare(String a, String b) {
      return getScore(b) - getScore(a);
   }

   private int scoreLetter(char c) {
      switch (c) {
         case 'a': case 'e': case 'i': case 'o': case 'u':
         case 'n': case 'r': case 't': case 'l': case 's':
            return 1;
         case 'd': case 'g':
            return 2;
         case 'b': case 'c': case 'm': case 'p':
            return 3;
         case 'f': case 'h': case 'v': case 'w': case 'y':
            return 4;
         case 'k':
            return 5;
         case 'j': case 'x':
            return 8;
         case 'q': case 'z':
            return 10;
      }
      return 0;
   }

   public int getScore(String s) {
      Integer result = cache.get(s);
      if (result == null) {
         final int len = s.length();
         int score = 0;
         for (int i = 0; i < len; i++)
            score += scoreLetter(s.charAt(i));
         return score;
      }
      return result;
   }
}
