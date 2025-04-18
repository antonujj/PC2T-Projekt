package me.antonujj.Backend;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentFileIO {

	public static void saveStudentToFile(Student student, File file) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("ID,Prijmeni,Jmeno,Obor,Datum narozeni,Znamky\n");

			StringBuilder sb = new StringBuilder();
			sb.append(student.getId()).append(",");
			sb.append(student.getPrijmeni()).append(",");
			sb.append(student.getJmeno()).append(",");
			sb.append(student.getObor()).append(",");
			sb.append(student.getNarozeni()).append(",");

			String znamky = "";
			for (int i = 0; i < student.getZnamky().size(); i++) {
				znamky += student.getZnamky().get(i);
				if (i != student.getZnamky().size() - 1) {
					znamky += ";";
				}
			}
			sb.append(znamky).append("\n");
			writer.write(sb.toString());
		}
	}

	public static Student importStudentFromFile(File file, int newId) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String header = br.readLine();
			String line = br.readLine();

			if (line != null) {
				String[] data = line.split(",");

				if (data.length < 5) {
					System.err.println("Chybny format souboru");
					return null;
				}

				String prijmeni = data[1].trim();
				String jmeno = data[2].trim();
				Obor obor = Obor.valueOf(data[3].trim());

				LocalDate narozeni = LocalDate.parse(data[4].trim());
				if (narozeni.isAfter(LocalDate.now())) {
					System.err.println("Datum narozeni novejsi, nez dnesek");
					return null;
				}

				List<Integer> znamky = new ArrayList<>();
				if (data.length >= 6 && !data[5].trim().isEmpty()) {
					String[] znamkyText = data[5].split(";");
					for (String z : znamkyText) {
						int znamka = Integer.parseInt(z.trim());
						if (znamka < 1 || znamka > 5) {
							System.err.println("Neplatna znamka: " + znamka);
							return null;
						}
						znamky.add(znamka);
					}
				}

				switch (obor) {
				case KYBERBEZPECNOST:
					return new KyberbezpecnostStudent(newId, prijmeni, jmeno, narozeni, obor, znamky);
				case TELEKOMUNIKACE:
					return new TelekomunikaceStudent(newId, prijmeni, jmeno, narozeni, obor, znamky);
				default:
					System.err.println("Neznamy obor");
					return null;
				}
			}
		} catch (Exception e) {
			System.out.println("Chyba pri nacitani: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
