package me.antonujj.Backend;

public class Morse {
	private static final String[] morseovka = { ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---",
			"-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--",
			"--..", "-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----.", "-----" };

	private static final char[] abeceda = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

	public static String toMorse(String text) {
		StringBuilder morse = new StringBuilder();
		text = text.toLowerCase();

		for (char c : text.toCharArray()) {
			if (c == ' ') {
				morse.append(" / ");
			} else {
				int index = new String(abeceda).indexOf(c);
				if (index != -1) {
					morse.append(morseovka[index]).append(" ");
				}
			}
		}

		return morse.toString().trim();
	}
}
