package de.dhbw.mannheim.cloudraid.fs;

import static de.dhbw.mannheim.cloudraid.fs.FileQueue.FileAction.CREATE;
import static de.dhbw.mannheim.cloudraid.fs.FileQueue.FileAction.DELETE;
import static de.dhbw.mannheim.cloudraid.fs.FileQueue.FileAction.MODIFY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFileSystemUtilities {
	private final static String TMP = System.getProperty("java.io.tmpdir")
			+ File.separator + "cloudraid-test" + File.separator;

	private static File tmpfile, subdir, file1, file2, file3, file4;

	@BeforeClass
	public static void oneTimeSetUp() throws IOException {
		tmpfile = new File(TMP);
		tmpfile.mkdirs();
		subdir = new File(TMP + "subdir");
		subdir.mkdir();
		file1 = new File(TMP + "file1");
		file1.createNewFile();
		file2 = new File(TMP + "file2");
		file2.createNewFile();
		file3 = new File(TMP + "subdir" + File.separator + "file3");
		file3.createNewFile();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		file3.delete();
		file2.delete();
		file1.delete();
		subdir.delete();
		tmpfile.delete();
	}

	@Test
	public void testRecursiveFileSystemWatcher() throws InterruptedException,
			IOException {
		RecursiveFileSystemWatcher rfsw = new RecursiveFileSystemWatcher(TMP,
				500);
		rfsw.start();
		Thread.sleep(2000);
		FileQueueEntry fqe;
		for (int i = 0; i < 3; i++) {
			assertFalse(FileQueue.isEmpty());
			fqe = FileQueue.get();
			String filename = fqe.getFileName();
			boolean isValidName = filename.equals(file1.getAbsolutePath())
					|| filename.equals(file2.getAbsolutePath())
					|| filename.equals(file3.getAbsolutePath());
			assertTrue(isValidName);
			assertTrue(fqe.getFileAction().equals(CREATE));
		}

		file4 = new File(TMP + File.separator + "subdir" + File.separator
				+ "file4");
		file4.createNewFile();
		Thread.sleep(1000);

		assertFalse(FileQueue.isEmpty());
		fqe = FileQueue.get();
		assertTrue(fqe.getFileName().equals(file4.getAbsolutePath()));
		assertTrue(fqe.getFileAction().equals(CREATE));
		assertTrue(FileQueue.isEmpty());

		file4.delete();
		file4.createNewFile();
		Thread.sleep(1000);

		assertFalse(FileQueue.isEmpty());
		fqe = FileQueue.get();
		assertTrue(fqe.getFileName().equals(file4.getAbsolutePath()));
		assertTrue(fqe.getFileAction().equals(MODIFY));
		assertTrue(FileQueue.isEmpty());

		file4.delete();
		Thread.sleep(1000);

		assertFalse(FileQueue.isEmpty());
		fqe = FileQueue.get();
		assertTrue(fqe.getFileName().equals(file4.getAbsolutePath()));
		assertTrue(fqe.getFileAction().equals(DELETE));
		assertTrue(FileQueue.isEmpty());

		rfsw.interrupt();
		rfsw.join();

		assertTrue(FileQueue.isEmpty());
	}

	@Test
	public void testFileManager() throws InterruptedException {
		FileQueue.add(file1.getAbsolutePath(), CREATE);
		FileQueue.add(file2.getAbsolutePath(), MODIFY);
		FileQueue.add(file3.getAbsolutePath(), DELETE);

		assertFalse(FileQueue.isEmpty());

		FileManager fm = new FileManager();
		fm.start();
		Thread.sleep(1000);
		fm.interrupt();
		fm.join();
		assertTrue(FileQueue.isEmpty());
	}

}
