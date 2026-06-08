package io.github.mohamedennahdi.virgulo.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import io.github.mohamedennahdi.virgulo.provider.SystemCarriageReturnProvider;
import net.sf.ennahdi.automatic.report.generator.generic.engine.Engine;
import net.sf.ennahdi.automatic.report.generator.generic.engine.enums.StatementType;
import net.sf.ennahdi.automatic.report.generator.generic.engine.exceptions.FileNotGeneratedException;
/**
 *
 * @author 	ENNAHDI EL IDRISSI, Mohamed
 * @version	1.0, 02 September 2016
 * @version	2.0, 05 June 2022
 * <p>Upgrading generic-generator to 2.2</p>
 * <p>Invoking Engine.executeStatement()<p>
 * <p>Converting CSVEngine from an abstract class to a concrete class</p>
 * <p>Adding program arguments (separator, includeHeaders)</p>
 * <p>Addressing potential resource leak with FileWriter object</p>
 * @version	3.0, 13 June 2022
 * <p>Upgrading generic-generator version to 3.0</p>
 * <p>Upgrading opencsv versuib to 5.6</p>
 * <p>File output path does not used "user.home" anymore.</p>
 * <p>Keeping one constructor to be orchestrated in the implementation of Builder design pattern.</p>
 */
public class CSVEngine extends Engine {
	private static final Logger logger = LogManager.getLogger(CSVEngine.class);

	private String path;
	private char separator;
	private char quote;
	private char escape;
	private boolean includeHeaders = true;

	public CSVEngine(Builder builder) {
		super(builder.connection, builder.statementType, builder.query, builder.arguments);
		this.includeHeaders = builder.includeHeaders;
		this.path = builder.path;
		if (Objects.isNull(builder.quote)) {
			this.quote = ICSVWriter.NO_QUOTE_CHARACTER;
		} else {
			this.quote = builder.quote;
		}
		if (Objects.isNull(builder.escape)) {
			this.escape = ICSVWriter.NO_ESCAPE_CHARACTER;
		} else {
			this.escape = builder.escape;
		}
		this.separator = builder.separator;
	}

	@Override
	public File generate() throws FileNotGeneratedException {
        File f = prepareFolders();
		CSVWriter writer = null;
		ResultSet rs = null;
        try (FileWriter fw = new FileWriter(f);) {
            logger.info("Generating  {}", f);
            rs = this.executeStatement(getConnection());

            String carriageReturn = new SystemCarriageReturnProvider().get();

            writer = new CSVWriter(fw, separator, quote, escape, carriageReturn);

            writer.writeAll(rs, includeHeaders);

            logger.info("{} Generated", f);
            return f;
        } catch (SQLException | IOException e) {
        	logger.error("", e);
        } finally {
            try {
                if (Objects.nonNull(rs)) {
                    rs.close();
                }
                this.getConnection().close();
            } catch (SQLException e) {
            	logger.error("", e);
            }
        }
        throw new FileNotGeneratedException("A problem occurred during the generation of the document " + this.path + ".");
	}

	private File prepareFolders() throws FileNotGeneratedException {
		File f = new File(this.path);
        f.getParentFile().mkdirs();
        boolean created = false;
		try {
			created = f.createNewFile();
		} catch (IOException e) {
			throw new FileNotGeneratedException("Problem occurred at the creation of File " + f + ". Generation aborted");
		}
		if (!created) {
			throw new FileNotGeneratedException("File " + f + " was not created. Generation aborted");
		}
		return f;
	}

	public static class Builder {

		Connection connection;
		StatementType statementType;
		String query;
		List<Object> arguments;


		private String path;
		private char separator = ',';
		private char quote;
		private char escape;
		private boolean includeHeaders = true;

		public Builder(Connection connection, String query, String path) {
			this.connection = connection;
			this.query = query;
			this.path = path;
		}

		public Builder statementType(StatementType statementType) {
			this.statementType = statementType;
			return this;
		}

		public Builder arguments(List<Object> arguments) {
			this.arguments = arguments;
			return this;
		}

		public Builder includeHeaders(boolean includeHeaders) {
			this.includeHeaders = includeHeaders;
			return this;
		}
		public Builder separator(char separator) {
			this.separator = separator;
			return this;
		}
		public Builder quote(char quote) {
			this.quote = quote;
			return this;
		}
		public Builder escape(char escape) {
			this.escape = escape;
			return this;
		}
		public CSVEngine build() {
			return new CSVEngine(this);
		}
	}
}
