package com.bp.criminalintent;
import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;

/* Save and load a crime.list */
public class CrimeListIO {
	private static final String FILE = "crime.list";

	public static void loadCrimes()
	{
		// read crimes from file and fill CrimeListSingleton
		Path file = FileSystems.getDefault().getPath("data", FILE);

		try (InputStream in = Files.newInputStream(file);
				BufferedReader reader =
						new BufferedReader(new InputStreamReader(in))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException x) {
			System.err.println(x);
		}		
	}

	public static void saveCrimes()
	{
		// save crimes to file TODO: for every crime, remove previous crime list
		byte data[] = null;
		Path p = null;
		p = Paths.get(FILE);
		try{
			Files.delete(p);;
		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", p);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", p);
		} catch (IOException x){
			// File permission problems are caught here.
			System.err.println(x);
		}
		String s = new String();

		CrimeListSingleton cls = CrimeListSingleton.getInstance();

		for (int i = 0; i < cls.getSize() ; i++)
		{
			Crime c = cls.getCrime(i);

			String.format("%d,%d", c.getmId().getMostSignificantBits(),
					c.getmId().getLeastSignificantBits());
			s += c.getmTitle();
			s += String.format(",%d,",  c.getmDate().getTimeInMillis());
			s += String.format("%d\n",  c.ismSolved() ? 1 : 0);

			data = s.getBytes(); // convert to a byte array

			// TODO: create "data" directory before write
			// TODO: do a for loop over all crimes in CrimeListSingleton
			//       and write them out to our data file
			// TODO: output in proper JSON format

			try (OutputStream out = new BufferedOutputStream(
					Files.newOutputStream(p, CREATE, APPEND))) {
				out.write(data, 0, data.length);
			} catch (IOException x) {
				System.err.println(x);
			}
		}		
	} 
}
