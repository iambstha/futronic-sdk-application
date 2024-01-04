package com.iambstha.futronicApp.extras;

import java.io.IOException;

import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.machinezoo.sourceafis.FingerprintTransparency;

public class MatchFingerprintWithSourceAFIS extends FingerprintTransparency {

	static boolean isMatching;
	static double threshold = 50;
	static double similarity;

	static byte[] probeImage = null;
	static byte[] candidateImage = null;

	public static void main(String[] args) {

	}

	public static boolean processFingerprint(byte[] probeImage, byte[] candidateImage) throws IOException {

		var candidate = new FingerprintTemplate(new FingerprintImage(candidateImage));

		try (var transparency = new MatchFingerprintWithSourceAFIS()) {

			var probe = new FingerprintTemplate(new FingerprintImage(probeImage));

			var matcher = new FingerprintMatcher(probe);

			similarity = matcher.match(candidate);

			isMatching = similarity >= threshold;

		}

		return isMatching;

	}

	@Override
	public void take(String key, String mime, byte[] data) {
		System.out.printf("%,9d B  %-17s %s\n", data.length, mime, key);
	}
}