package io.github.mohamedennahdi.virgulo.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import net.sf.ennahdi.automatic.report.generator.generic.engine.Engine;


@Testcontainers
public class CSVEngineTest {

	private final Logger logger = LogManager.getLogger(CSVEngineTest.class);

	@TempDir(cleanup = CleanupMode.NEVER)
	File tempDir;

	@Container
	private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36").withDatabaseName("testdb")
																					   .withUsername("testuser")
																					   .withPassword("testpass")
													 								   .withInitScript("db/data-ourC58bC3ig3Tc6khxGOZ.sql");
	@Test
	void generate_with_default_settings_Test() throws Exception {
		char separator = ',';
		try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(),mysql.getUsername(),mysql.getPassword())) {
			Engine engine = new CSVEngine.Builder(c, "SELECT * FROM myTable", tempDir + "/employees.csv").build();
			File testSubject = engine.generate();
			logger.info("Generated file: {}", testSubject);

			assertNotNull(testSubject);
			assertTrue(testSubject.length() > 0);

	        List<String[]> rows = readCSV(testSubject, separator);
            assertThat(rows).hasSize(501);
            assertThat(rows.get(0)[0]).contains("id");
            assertThat(rows.get(0)[1]).contains("name");
            assertThat(rows.get(0)[2]).contains("phone");
            assertThat(rows.get(0)[3]).contains("country");
            assertThat(rows.get(0)[4]).contains("currency");
            assertThat(rows.get(1)[1]).contains("Jolene Hubbard");
            assertThat(rows.get(250)[1]).contains("Cain Cleveland");
            assertThat(rows.get(176)[4]).contains("$54.30");
		}
	}

	@Test
	void generate_with_semicolumn_Test() throws Exception {
		char separator = ';';
		try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(),mysql.getUsername(),mysql.getPassword())) {
			Engine engine = new CSVEngine.Builder(c, "SELECT * FROM myTable", tempDir + "/employees.csv").separator(separator).build();
			File testSubject = engine.generate();
			logger.info("Generated file: {}", testSubject);

			assertNotNull(testSubject);
			assertTrue(testSubject.length() > 0);

			List<String[]> rows = readCSV(testSubject, separator);
			assertThat(rows).hasSize(501);
			assertThat(rows.get(0)[0]).contains("id");
			assertThat(rows.get(0)[1]).contains("name");
			assertThat(rows.get(0)[2]).contains("phone");
			assertThat(rows.get(0)[3]).contains("country");
			assertThat(rows.get(0)[4]).contains("currency");
			assertThat(rows.get(1)[1]).contains("Jolene Hubbard");
			assertThat(rows.get(250)[1]).contains("Cain Cleveland");
			assertThat(rows.get(176)[4]).contains("$54.30");
		}
	}

	@Test
	void generate_with_semicolumn_and_quote_and_Test() throws Exception {
		char separator = ';';
		try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(),mysql.getUsername(),mysql.getPassword())) {
			Engine engine = new CSVEngine.Builder(c, "SELECT * FROM myTable", tempDir + "/employees.csv").separator(separator).quote('\'').escape('\'').build();
			File testSubject = engine.generate();
			logger.info("Generated file: {}", testSubject);

			assertNotNull(testSubject);
			assertTrue(testSubject.length() > 0);

			List<String[]> rows = readCSV(testSubject, separator);
			assertThat(rows).hasSize(501);
			assertThat(rows.get(0)[0]).contains("id");
			assertThat(rows.get(0)[1]).contains("name");
			assertThat(rows.get(0)[2]).contains("phone");
			assertThat(rows.get(0)[3]).contains("country");
			assertThat(rows.get(0)[4]).contains("currency");
			assertThat(rows.get(1)[1]).contains("Jolene Hubbard");
			assertThat(rows.get(250)[1]).contains("Cain Cleveland");
			assertThat(rows.get(176)[4]).contains("$54.30");
			assertThat(rows.get(172)[1]).contains("'Marvin O''brien'");
		}
	}

	@Test
	void generate_without_header_Test() throws Exception {
		char separator = ',';
		try (Connection c = DriverManager.getConnection(mysql.getJdbcUrl(),mysql.getUsername(),mysql.getPassword())) {
			Engine engine = new CSVEngine.Builder(c, "SELECT * FROM myTable", tempDir + "/employees.csv").includeHeaders(false).build();
			File testSubject = engine.generate();
			logger.info("Generated file: {}", testSubject);

			assertNotNull(testSubject);
			assertTrue(testSubject.length() > 0);

	        List<String[]> rows = readCSV(testSubject, separator);
            assertThat(rows).hasSize(500);
            assertThat(rows.get(0)[1]).contains("Jolene Hubbard");
            assertThat(rows.get(249)[1]).contains("Cain Cleveland");
            assertThat(rows.get(175)[4]).contains("$54.30");
		}
	}

    private List<String[]> readCSV(File file, char separator) throws IOException, CsvException {
    	CSVParser parser = new CSVParserBuilder().withSeparator(separator).build();
        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(parser).build()) {
            return reader.readAll();
        }
    }
}