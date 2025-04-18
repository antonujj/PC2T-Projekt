package me.antonujj.Backend;

import java.time.LocalDate;
import java.util.List;

public class TelekomunikaceStudent extends Student {

	public TelekomunikaceStudent(int id, String prijmeni, String jmeno, LocalDate narozeni, Obor obor,
			List<Integer> znamky) {
		super(id, prijmeni, jmeno, narozeni, obor, znamky);
	}

}
