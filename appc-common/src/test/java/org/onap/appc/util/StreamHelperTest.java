/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.onap.appc.util.StreamHelper;

public class StreamHelperTest {

    private static final String text = "Filler text (also placeholder text or dummy text) is text that shares "
        + "some characteristics of a real written text, but is random or otherwise generated. It may be used "
        + "to display a sample of fonts, generate text for testing, or to spoof an e-mail spam filter. The "
        + "process of using filler text is sometimes called greeking, although the text itself may be nonsense, "
        + "or largely Latin, as in Lorem ipsum.\nASDF is the sequence of letters that appear on the first four "
        + "keys on the home row of a QWERTY or QWERTZ keyboard. They are often used as a sample or test case "
        + "or as random, meaningless nonsense. It is also a common learning tool for keyboard classes, since "
        + "all four keys are located on Home row.\nETAOIN SHRDLU is the approximate order of frequency of the "
        + "twelve most commonly used letters in the English language, best known as a nonsense phrase that "
        + "sometimes appeared in print in the days of \"hot type\" publishing due to a custom of Linotype "
        + "machine operators.\nLorem ipsum... is one of the most common filler texts, popular with "
        + "typesetters and graphic designers. \"Li Europan lingues...\" is another similar example.\n"
        + "Now is the time for all good men to come to the aid of the party\" is a phrase first proposed "
        + "as a typing drill by instructor Charles E. Weller; its use is recounted in his book The Early "
        + "History of the Typewriter, p. 21 (1918).[1] Frank E. McGurrin, an expert on the early Remington "
        + "typewriter, used it in demonstrating his touch typing abilities in January 1889.[2] It has "
        + "appeared in a number of typing books, often in the form \"Now is the time for all good men to "
        + "come to the aid of their country.\"\nThe quick brown fox jumps over the lazy dog - A coherent, "
        + "short phrase that uses every letter of the alphabet. See pangram for more examples.\nNew Petitions"
        + " and Building Code - Many B movies of the 1940s, 50s, and 60s utilized the \"spinning newspaper\" "
        + "effect to narrate important plot points that occurred offscreen. The effect necessitated the "
        + "appearance of a realistic front page, which consisted of a main headline relevant to the plot, "
        + "and several smaller headlines used as filler. A large number of these spinning newspapers "
        + "included stories titled \"New Petitions Against Tax\" and \"Building Code Under Fire.\" These "
        + "phrases have become running jokes among B movie fans, and particularly fans of Mystery "
        + "Science Theater 3000. \nCharacter Generator Protocol - The Character Generator Protocol "
        + "(CHARGEN) service is an Internet protocol intended for testing, debugging, and measurement "
        + "purposes.\n!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefgh\n\""
        + "#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghi\n"
        + "#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghij\n"
        + "$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijk\n";

    @Test
    public void should_return_empty_string_when_given_null_input_stream() {
        assertNotNull(StreamHelper.getStringFromInputStream(null));
        assertEquals("", StreamHelper.getStringFromInputStream(null));
    }

    @Test
    public void should_return_empty_string_when_given_empty_byte_array() {
        ByteArrayInputStream emptyInputStream = new ByteArrayInputStream(new byte[0]);

        assertEquals("", StreamHelper.getStringFromInputStream(emptyInputStream));
    }

    @Test
    public void should_return_string_when_given_byte_array() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());

        assertEquals(text, StreamHelper.getStringFromInputStream(inputStream));
    }
}
