package de.dhbw.mannheim.cloudraid.fs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFileLock {
	@BeforeClass
	public static void oneTimeSetUp() {

	}

	@AfterClass
	public static void oneTimeTearDown() {

	}

	@Test
	public void testLock() {
		FileManager fm1 = new FileManager();
		FileManager fm2 = new FileManager();

		assertTrue(FileLock.lock("string", fm1));

		assertFalse(FileLock.lock("string", fm2));

		assertFalse(FileLock.unlock("string", fm2));

		assertTrue(FileLock.unlock("string", fm1));

		assertTrue(FileLock.lock("string", fm2));

		assertTrue(FileLock.lock("another-string", fm1));
		
		assertTrue(FileLock.unlock("string", fm2));
		
		assertFalse(FileLock.unlock("string", fm2));
	}

}