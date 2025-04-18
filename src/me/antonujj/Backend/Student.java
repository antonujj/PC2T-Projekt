package me.antonujj.Backend;

import java.time.LocalDate;
import java.util.List;

public abstract class Student {

	private int id;
	private String jmeno;
	private String prijmeni;
	private LocalDate narozeni;
	private List<Integer> znamky;
	final private Obor obor;

	public Student(int id, String prijmeni, String jmeno, LocalDate narozeni, Obor obor, List<Integer> znamky) {
		super();
		this.id = id;
		this.jmeno = jmeno;
		this.prijmeni = prijmeni;
		this.narozeni = narozeni;
		this.obor = obor;
		this.znamky = znamky;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getJmeno() {
		return jmeno;
	}

	public void setJmeno(String jmeno) {
		this.jmeno = jmeno;
	}

	public String getPrijmeni() {
		return prijmeni;
	}

	public void setPrijmeni(String prijmeni) {
		this.prijmeni = prijmeni;
	}

	public LocalDate getNarozeni() {
		return narozeni;
	}

	public void setNarozeni(LocalDate narozeni) {
		this.narozeni = narozeni;
	}

	public List<Integer> getZnamky() {
		return znamky;
	}

	public void setZnamky(List<Integer> znamky) {
		this.znamky = znamky;
	}

	public Obor getObor() {
		return obor;
	}

	public double getPrumer() {
		if (znamky == null || znamky.isEmpty()) {
			return 0.0;
		}

		int suma = 0;
		for (int znamka : znamky) {
			suma += znamka;
		}
		return suma / (double) znamky.size();
	}

}
