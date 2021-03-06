package dynsim.math.analysis.signal;

/**
 * Fft.java
 * 
 * From the Unix Version 2.4 by Steve Sampson, Public Domain, September 1988.
 * Adapted for Java by Ben Stoltz <stoltz@sun.com>, September 1997
 * *********************** Further adapted by Alan Murphy as part of MSc in
 * Multimedia Technology at University College Cork - email: revgen@indigo.ie
 * *********************** Refer to http://www.neato.org/~ben/Fft.html for
 * updates and related resources.
 * 
 * (Some of the comments from original source:
 * 
 * This program produces a Frequency Domain display from the Time Domain data
 * input; using the Fast Fourier Transform.
 * 
 * The Real data is generated by the in-phase (I) channel, and the Imaginary
 * data is produced by the quadrature-phase (Q) channel of a Doppler Radar
 * receiver. The middle filter is zero Hz. Closing targets are displayed to the
 * right, and Opening targets to the left.
 * 
 * Note: With Imaginary data set to zero the output is a mirror image. )
 * 
 */

public class Fft {
	/*
	 * Precalculated values
	 */
	private double SampleRate; // sample rate for displaying

	private int NSamples; // must be a power of 2

	private int Power; // log2 of NSamples

	private double[] Freq; // Frequency represented by each bin in Spectra

	private int[] Permute; // bit reversing permutation table

	private double[] Sines; // pre-computed table of sines

	/*
	 * Temporary values
	 */
	private double[] Real; // Temporary: Real part

	private double[] Imag; // Temporary: Imaginary part

	/*
	 * Outputs
	 */
	private double[] Spectra;// Fft output

	private double[] Max; // value of top N frequencies in Spectra

	private int[] Index; // index of top N frequencies in Spectra

	/**
	 * Display the frequency domain.
	 */
	public String toString() {
		double hival = (double) 0.0; // maximum value for scaling bar-chart
		final int bigbar = 40; // characters in max bar length

		for (int i = 0; i < Spectra.length; ++i) {
			if (Spectra[i] > hival) {
				hival = Spectra[i];
			}
		}
		if (hival == 0.0)
			hival = 1.0;

		int loop;
		int x;

		StringBuffer sb = new StringBuffer();

		for (loop = 0; loop < Spectra.length; loop++) {
			sb.append(Freq[loop] + "\t|");
			// print a histogram bar
			x = (int) (Spectra[loop] * bigbar / hival);
			for (int i = 0; i < x; ++i) {
				sb.append('=');
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	/**
	 * Initialization routine precalculates information in order to speed up
	 * subsequent FFT calculations.
	 * 
	 * @param rate
	 *            Sample rate of input data
	 * @param nsamples
	 *            Number of samples to FFT at a time. Must be a power of two.
	 * @param topn
	 *            The N biggest FFT bins are collected in the array "Max"
	 */
	public Fft(double rate, int nsamples, int topn) throws Exception {
		SampleRate = rate;
		NSamples = nsamples;

		// Input data array length - must be a power of two
		Power = (int) (Math.log((double) NSamples) / Math.log(2.0));
		if ((1 << Power) != NSamples) {
			throw new Exception(); // XXX FooException()?
		}

		/*
		 * Build table of sines. The table is a sampling of sin(x) for x = 0 to
		 * 2pi step d, where d is 2pi/N. N is the total number of samples.
		 */
		Sines = new double[NSamples];
		for (int i = 0; i < Sines.length; i++) {
			Sines[i] = (double) Math.sin((double) (i * (2 * Math.PI) / Sines.length));
		}

		// A place to hold the data
		Real = new double[NSamples];
		Imag = new double[NSamples];

		// Resulting FFT is put in Spectra
		Spectra = new double[NSamples / 2];

		// Scan for largest magnitude freqencies and place in Max[]
		Max = new double[topn]; // collect value of top N frequencies
		Index = new int[topn]; // collect index of top N frequencies

		// Build the bit reversal lookup table
		Permute = new int[NSamples];
		int result;
		for (int index = 0; index < NSamples; index++) {
			result = 0;
			for (int loop = 0; loop < Power; loop++) {
				if ((index & (1 << loop)) != 0) {
					result |= 1 << (Power - 1 - loop);
				}
			}
			Permute[index] = result;
		}

		Freq = new double[NSamples / 2];
		for (int index = 0; index < NSamples / 2; ++index) {
			Freq[index] = (SampleRate * index + Spectra.length) / (Spectra.length * 2);
		}
	}

	public void calculate(double[] rdata, double[] idata) {
		/*
		 * Scale the data
		 */
		for (int i = 0; i < rdata.length; ++i) {
			// Scale input data
			Real[i] = (double) rdata[i] / (double) NSamples;
			Imag[i] = (double) idata[i] / (double) NSamples;
		}
		runfft();
	}

	public void calculate(double[] rdata) {
		/*
		 * Scale the data and set the imaginary part to zero.
		 */
		for (int i = 0; i < rdata.length; ++i) {
			// Scale input data
			Real[i] = (double) rdata[i] / (double) NSamples;
			Imag[i] = 0.0;
		}
		runfft();
	}

	private void runfft() {
		// begin FFT
		int i1 = NSamples / 2;
		int i2 = 1;

		/* perform the butterfly's */

		for (int loop = 0; loop < Power; loop++) {
			int i3 = 0;
			int i4 = i1;
			int y;
			double z1;
			double z2;

			for (int loop1 = 0; loop1 < i2; loop1++) {
				/*
				 * if (i1 == 0) { System.out.println("loop="+loop+ "
				 * loop1="+loop1+ " Power="+Power+ " i1="+i1+ " i2="+i2); }
				 */

				y = Permute[i3 / i1];
				z1 = Sines[((y) + (Real.length >> 2)) % Real.length]; // cosine
				z2 = -Sines[y];

				double a1;
				double a2;
				double b1;
				double b2;
				for (int loop2 = i3; loop2 < i4; loop2++) {

					a1 = Real[loop2];
					a2 = Imag[loop2];

					b1 = z1 * Real[loop2 + i1] - z2 * Imag[loop2 + i1];
					b2 = z2 * Real[loop2 + i1] + z1 * Imag[loop2 + i1];

					Real[loop2] = a1 + b1;
					Imag[loop2] = a2 + b2;

					Real[loop2 + i1] = a1 - b1;
					Imag[loop2 + i1] = a2 - b2;
				}

				i3 += (i1 << 1);
				i4 += (i1 << 1);
			}

			i1 >>= 1;
			i2 <<= 1;
		}
		// end of FFT

		int p;
		for (int i = 0; i < Spectra.length; i++) {
			p = Permute[i];

			// Calculate power magnitude
			Spectra[i] = (Math.sqrt(Real[p] * Real[p] + Imag[p] * Imag[p]));
			// Spectra[i] = 10*Math.log10(Math.sqrt(Real[p] * Real[p] + Imag[p]
			// * Imag[p])/0.0002);
		}

		/*
		 * Scan for biggest N values in Spectra
		 */
		// double[] sumSpec = new double[nsamples/2]; // XXX Total Energy?
		for (int i = 0; i < Spectra.length; ++i) {
			// sumSpec[i] += Spectra[i];
			if (Spectra[i] > Max[Max.length - 1]) {
				for (int j = 0; j < Max.length; ++j) {
					if (Spectra[i] > Max[j]) {
						for (int k = Max.length - 1; k > j; --k) {
							Max[k] = Max[k - 1];
							Index[k] = Index[k - 1];
						}
						Max[j] = Spectra[i];
						Index[j] = i;
					}
				}
			}
		}
	}

	public double[] getSpectra() {
		return Spectra;
	}

	public double[] getFreq() {
		return Freq;
	}

	public double[] getMax() {
		return Max;
	}

	public int[] getIndex() {
		return Index;
	}

}
