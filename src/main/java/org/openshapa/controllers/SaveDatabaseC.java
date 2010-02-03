package org.openshapa.controllers;

import org.openshapa.OpenSHAPA;
import org.openshapa.models.db.DataCell;
import org.openshapa.models.db.DataColumn;
import org.openshapa.models.db.LogicErrorException;
import org.openshapa.models.db.MacshapaDatabase;
import org.openshapa.models.db.SystemErrorException;
import org.openshapa.util.FileFilters.CSVFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.filechooser.FileFilter;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.openshapa.models.db.Database;
import org.openshapa.models.db.FormalArgument;
import org.openshapa.models.db.MatrixVocabElement;
import org.openshapa.models.db.MatrixVocabElement.MatrixType;
import org.openshapa.models.db.PredicateVocabElement;
import org.openshapa.util.FileFilters.MODBFilter;

/**
 * Controller for saving the database to disk.
 */
public final class SaveDatabaseC {

    /** Logger for this class. */
    private static Logger logger = Logger.getLogger(SaveDatabaseC.class);

    /**
     * Constructor.
     *
     * @param destinationFile The destination to use when saving the c
     */
    public SaveDatabaseC(final File destinationFile) {
        // We bypass any overwrite checks here.
        String outputFile = destinationFile.getName().toLowerCase();
        String extension = outputFile.substring(outputFile.lastIndexOf('.'),
                                                outputFile.length());

        try {
            if (extension.equals(".csv")) {
                saveAsCSV(destinationFile.toString());
            } else if (extension.equals(".odb")) {
                saveAsMacSHAPADB(destinationFile.toString());
            }
        } catch (LogicErrorException le) {
            OpenSHAPA.getApplication().showWarningDialog(le);
        }
    }

    /**
     * Constructor.
     *
     * @param destinationFile The destination to use when saving the database.
     * @param fileFilter The selected filter to use when saving the database.
     */
    public SaveDatabaseC(final String destinationFile,
                         final FileFilter fileFilter) {

        String outputFile = destinationFile.toLowerCase();

        if (fileFilter.getClass() == CSVFilter.class) {
            // BugzID:541 - Don't append ".csv" unless needed.
            if (!outputFile.contains(".csv")) {
                outputFile = destinationFile.concat(".csv");
            }

        } else if (fileFilter.getClass() == MODBFilter.class) {
            // Don't append ".db" if the path already contains it.
            if (!outputFile.contains(".odb")) {
                outputFile = destinationFile.concat(".odb");
            }
        }

        File outFile = new File(outputFile);

        // BugzID:449 - Set filename in spreadsheet window and database to
        // be the same as the file specified.
        try {
            // Check for existence; if so, confirm overwrite.
            if ((outFile.exists()
                 && OpenSHAPA.getApplication().overwriteExisting())
                 || !outFile.exists()) {

                if (fileFilter.getClass() == CSVFilter.class) {
                    saveAsCSV(outputFile);

                } else if (fileFilter.getClass() == MODBFilter.class) {
                    saveAsMacSHAPADB(outputFile);
                }

                String dbName;
                if (outFile.getName().lastIndexOf('.') != -1) {
                    dbName = outFile.getName().substring(0, outFile.getName()
                                                             .lastIndexOf('.'));
                } else {
                    dbName = outFile.getName();
                }
                OpenSHAPA.getDB().setName(dbName);
                OpenSHAPA.getDB().setSourceFile(outFile);

                // Update the name of the window to include the name we just
                // set in the database.
                OpenSHAPA.getApplication().updateTitle();
            }
        } catch (SystemErrorException se) {
            logger.error("Can't set db name to specified file.", se);
        } catch (LogicErrorException le) {
            OpenSHAPA.getApplication().showWarningDialog(le);
        }
    }

    /**
     * Saves the database to the specified destination in a MacSHAPA format.
     *
     * @param outFile The path of the file to use when writing to disk.
     *
     * @throws LogicErrorException When unable to save the database as a
     * macshapa database to disk (usually because of permissions errors).
     */
    public void saveAsMacSHAPADB(final String outFile)
    throws LogicErrorException {

        try {
            PrintStream outStream = new PrintStream(outFile);
            OpenSHAPA.getDB().toMODBFile(outStream, "\r");
            outStream.close();

            Database db = OpenSHAPA.getDB();

            // BugzID:743 - Here we update the GUI to indicate successful save
            db.saveDatabase();

        } catch (FileNotFoundException e) {
            ResourceMap rMap = Application.getInstance(OpenSHAPA.class)
                                          .getContext()
                                          .getResourceMap(OpenSHAPA.class);
            throw new LogicErrorException(rMap.getString("UnableToSave.message",
                                          outFile), e);
        } catch (IOException e) {
            ResourceMap rMap = Application.getInstance(OpenSHAPA.class)
                                          .getContext()
                                          .getResourceMap(OpenSHAPA.class);
            throw new LogicErrorException(rMap.getString("UnableToSave.message",
                                          outFile), e);
        } catch (SystemErrorException e) {
            logger.error("Can't write macshapa db file '" + outFile + "'", e);
        }
    }

    /**
     * Saves the database to the specified destination in a CSV format.
     *
     * @param outFile The path of the file to use when writing to disk.
     *
     * @throws LogicErrorException When unable to save the database as a CSV
     * to disk (usually because of permissions errors).
     */
    public void saveAsCSV(final String outFile) throws LogicErrorException {
        MacshapaDatabase db = OpenSHAPA.getDB();

        try {
            FileWriter fileWriter = new FileWriter(outFile);
            BufferedWriter out = new BufferedWriter(fileWriter);
            // Dump out an identifier for the version of file.
            out.write("#2");
            out.newLine();

            // Dump out all the predicates held within the database.
            Vector<PredicateVocabElement> predicates = db.getPredVEs();
            if (predicates.size() > 0) {
                int counter = 0;

                for (PredicateVocabElement pve : predicates) {
                    out.write(counter + ":" + pve.getName() + "-");
                    for (int j = 0; j < pve.getNumFormalArgs(); j++) {
                        FormalArgument fa = pve.getFormalArgCopy(j);
                        String name = fa.getFargName()
                                   .substring(1, fa.getFargName().length() - 1);
                        out.write(name + "|" + fa.getFargType().toString());

                        if (j < pve.getNumFormalArgs() - 1) {
                            out.write(",");
                        }
                    }

                    out.newLine();
                    counter++;
                }
            }

            // Dump out the data from all the columns.
            Vector<Long> colIds = db.getColOrderVector();
            for (int i = 0; i < colIds.size(); i++) {
                DataColumn dc = db.getDataColumn(colIds.get(i));
                boolean isMatrix = false;
                out.write(dc.getName() + " (" + dc.getItsMveType() + ")");

                // If we a matrix type - we need to dump the formal args.
                MatrixVocabElement mve = db.getMatrixVE(dc.getItsMveID());
                if (dc.getItsMveType() == MatrixType.MATRIX) {
                    isMatrix = true;
                    out.write("-");
                    for (int j = 0; j < mve.getNumFormalArgs(); j++) {
                        FormalArgument fa = mve.getFormalArgCopy(j);
                        String name = fa.getFargName()
                                   .substring(1, fa.getFargName().length() - 1);
                        out.write(name + "|" + fa.getFargType().toString());

                        if (j < mve.getNumFormalArgs() - 1) {
                            out.write(",");
                        }
                    }
                }

                out.newLine();
                for (int j = 1; j <= dc.getNumCells(); j++) {
                    DataCell c = (DataCell) dc.getDB().getCell(dc.getID(), j);
                    out.write(c.getOnset().toString());
                    out.write(",");
                    out.write(c.getOffset().toString());
                    out.write(",");
                    String value = c.getVal().toEscapedString();

                    if (!isMatrix) {
                        value = value.substring(1, value.length() - 1);
                    }
                    out.write(value);
                    out.newLine();
                }
            }
            out.close();
            fileWriter.close();

            // BugzID:743 - Here we update the GUI to indicate successful save
            db.saveDatabase();

        } catch (FileNotFoundException e) {
            ResourceMap rMap = Application.getInstance(OpenSHAPA.class)
                                          .getContext()
                                          .getResourceMap(OpenSHAPA.class);
            throw new LogicErrorException(rMap.getString("UnableToSave.message",
                                          outFile), e);
        } catch (IOException e) {
            ResourceMap rMap = Application.getInstance(OpenSHAPA.class)
                                          .getContext()
                                          .getResourceMap(OpenSHAPA.class);
            throw new LogicErrorException(rMap.getString("UnableToSave.message",
                                          outFile), e);
        } catch (SystemErrorException se) {
            logger.error("Unable to save database as CSV file", se);
        }
    }
}
