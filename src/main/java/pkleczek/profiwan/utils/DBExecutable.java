package pkleczek.profiwan.utils;

import java.sql.SQLException;


public interface DBExecutable {
	void insertDBEntry() throws SQLException;
	void updateDBEntry() throws SQLException;
	void deleteDBEntry() throws SQLException;
}
