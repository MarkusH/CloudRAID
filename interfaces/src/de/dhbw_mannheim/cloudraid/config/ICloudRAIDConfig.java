/*
 * Copyright 2011 - 2012 by the CloudRAID Team
 * see AUTHORS for more details
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.dhbw_mannheim.cloudraid.config;

import java.util.HashMap;
import java.util.NoSuchElementException;

import de.dhbw_mannheim.cloudraid.config.exceptions.InvalidConfigValueException;
import de.dhbw_mannheim.cloudraid.config.exceptions.MissingConfigValueException;

/**
 * An interface for a configuration storage that supports encryption.
 * 
 * @author Markus Holtermann
 */
public interface ICloudRAIDConfig {

	/**
	 * Permanently remove the configuration.
	 * 
	 * @return True, if the configuration has been removed.
	 */
	public boolean delete();

	/**
	 * Returns the {@link Boolean} representation of the value of a stored key,
	 * if it is possible. If not, the internal default value is returned.<br>
	 * If neither the internal default value can be found nor the key exists, a
	 * {@link MissingConfigValueException} is thrown.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @return The {@link Boolean} representation of the value
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found and has no
	 *             internal default value
	 * 
	 * @see #getBoolean(String, Boolean)
	 */
	public boolean getBoolean(String key) throws MissingConfigValueException;

	/**
	 * Returns the {@link Boolean} representation of the value of a stored key,
	 * if it is possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The {@link Boolean} representation of the value or
	 *         <code>defaultVal</code>
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found nor the given
	 *             default value is != null
	 * @throws InvalidConfigValueException
	 *             Thrown if the decryption process fails
	 * 
	 * @see #getBoolean(String)
	 */
	public boolean getBoolean(String key, Boolean defaultVal)
			throws MissingConfigValueException, InvalidConfigValueException;

	/**
	 * @return Returns the CloudRAID user home path
	 */
	public String getCloudRAIDHome();

	/**
	 * @return Returns the config path
	 */
	public String getConfigPath();

	/**
	 * Returns the default data for this configuration.
	 * 
	 * @return the defaultData
	 */
	public HashMap<String, String> getDefaultData();

	/**
	 * Returns the {@link Double} representation of the value of a stored key,
	 * if it is possible. If not, the internal default value is returned.<br>
	 * If neither the internal default value can be found nor the key exists, a
	 * {@link MissingConfigValueException} is thrown.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @return The {@link Double} representation of the value
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found and has no
	 *             internal default value
	 * 
	 * @see #getDouble(String, Double)
	 */
	public double getDouble(String key) throws MissingConfigValueException;

	/**
	 * Returns the {@link Double} representation of the value of a stored key,
	 * if it is possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The {@link Double} representation of the value or
	 *         <code>defaultVal</code>
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found nor the given
	 *             default value is != null
	 * @throws InvalidConfigValueException
	 *             Thrown if the decryption process fails
	 * 
	 * @see #getDouble(String)
	 */
	public double getDouble(String key, Double defaultVal)
			throws MissingConfigValueException, InvalidConfigValueException;

	/**
	 * Returns the {@link Float} representation of the value of a stored key, if
	 * it is possible. If not, the internal default value is returned.<br>
	 * If neither the internal default value can be found nor the key exists, a
	 * {@link MissingConfigValueException} is thrown.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @return The {@link Float} representation of the value
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found and has no
	 *             internal default value
	 * 
	 * @see #getFloat(String, Float)
	 */
	public float getFloat(String key) throws MissingConfigValueException;

	/**
	 * Returns the {@link Float} representation of the value of a stored key, if
	 * it is possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The {@link Float} representation of the value or
	 *         <code>defaultVal</code>
	 * 
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found nor the given
	 *             default value is != null
	 * @throws InvalidConfigValueException
	 *             Thrown if the decryption process fails
	 * 
	 * @see #getFloat(String)
	 */
	public float getFloat(String key, Float defaultVal)
			throws MissingConfigValueException, InvalidConfigValueException;

	/**
	 * Returns the {@link Integer} representation of the value of a stored key,
	 * if it is possible. If not, the internal default value is returned.<br>
	 * If neither the internal default value can be found nor the key exists, a
	 * {@link MissingConfigValueException} is thrown.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @return The {@link Integer} representation of the value
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found and has no
	 *             internal default value
	 * 
	 * @see #getInt(String, Integer)
	 */
	public int getInt(String key) throws MissingConfigValueException;

	/**
	 * Returns the {@link Integer} representation of the value of a stored key,
	 * if it is possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The {@link Integer} representation of the value or
	 *         <code>defaultVal</code>
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found nor the given
	 *             default value is != null
	 * @throws InvalidConfigValueException
	 *             Thrown if the decryption process fails
	 * 
	 * @see #getInt(String)
	 */
	public int getInt(String key, Integer defaultVal)
			throws MissingConfigValueException, InvalidConfigValueException;

	/**
	 * Returns the {@link Long} representation of the value of a stored key, if
	 * it is possible. If not, the internal default value is returned.<br>
	 * If neither the internal default value can be found nor the key exists, a
	 * {@link MissingConfigValueException} is thrown.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @return The {@link Long} representation of the value
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found and has no
	 *             internal default value
	 * 
	 * @see #getLong(String, Long)
	 */
	public long getLong(String key) throws MissingConfigValueException;

	/**
	 * Returns the {@link Long} representation of the value of a stored key, if
	 * it is possible. If not, the <code>defaultVal</code> is returned. <br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The {@link Long} representation of the value or
	 *         <code>defaultVal</code>
	 * @throws NoSuchElementException
	 *             Thrown if neither the given key can be found nor the given
	 *             default value is != null
	 * @throws InvalidConfigValueException
	 *             Thrown if the decryption process fails
	 * 
	 * @see #getLong(String)
	 */
	public long getLong(String key, Long defaultVal)
			throws NoSuchElementException, InvalidConfigValueException;

	/**
	 * Returns the {@link String} representation of the value of a stored key,
	 * if it is possible. If not, the internal default value is returned.<br>
	 * If neither the internal default value can be found nor the key exists, a
	 * {@link MissingConfigValueException} is thrown.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @return The {@link String} representation of the value
	 * @throws MissingConfigValueException
	 *             Thrown if neither the given key can be found and has no
	 *             internal default value
	 * 
	 * @see #getString(String, String)
	 */
	public String getString(String key) throws MissingConfigValueException;

	/**
	 * Returns the {@link String} representation of the value of a stored key.<br>
	 * If the key does not exist <code>defaultVal</code> is returned.
	 * 
	 * @param key
	 *            The key of the value in the Config.
	 * @param defaultVal
	 *            The fall back value.
	 * @return The {@link String} representation of the value or
	 *         <code>defaultVal</code>
	 * @throws NoSuchElementException
	 *             Thrown if neither the given key can be found nor the given
	 *             default value is != null
	 * @throws InvalidConfigValueException
	 *             Thrown if the decryption process fails
	 * 
	 * @see #getString(String)
	 */
	public String getString(String key, String defaultVal)
			throws NoSuchElementException, InvalidConfigValueException;

	/**
	 * Initialize the config with a password
	 * 
	 * @param password
	 *            The users masterpassword
	 * @return returns the instance
	 */
	public ICloudRAIDConfig init(String password);

	/**
	 * Checks if there <code>key</code> is set.
	 * 
	 * @param key
	 *            The key to check.
	 * @return <code>true</code> if it exists.
	 */
	public boolean keyExists(String key);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @see #put(String, String)
	 */
	public void put(String key, boolean value);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @param encrypted
	 *            <code>true</code> if the entry shall be stored encrypted
	 * @see #put(String, String, boolean)
	 */
	public void put(String key, boolean value, boolean encrypted);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @see #put(String, String)
	 */
	public void put(String key, double value);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @param encrypted
	 *            <code>true</code> if the entry shall be stored encrypted
	 * @see #put(String, String, boolean)
	 */
	public void put(String key, double value, boolean encrypted);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @see #put(String, String)
	 */
	public void put(String key, float value);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @param encrypted
	 *            <code>true</code> if the entry shall be stored encrypted
	 * @see #put(String, String, boolean)
	 */
	public void put(String key, float value, boolean encrypted);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @see #put(String, String)
	 */
	public void put(String key, int value);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @param encrypted
	 *            <code>true</code> if the entry shall be stored encrypted
	 * @see #put(String, String, boolean)
	 */
	public void put(String key, int value, boolean encrypted);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @see #put(String, String)
	 */
	public void put(String key, long value);

	/**
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 * @param encrypted
	 *            <code>true</code> if the entry shall be stored encrypted
	 * @see #put(String, String, boolean)
	 */
	public void put(String key, long value, boolean encrypted);

	/**
	 * Sets the value of a certain parameter in the config. When the config is
	 * written to a file the last value that was set for the <code>key</code> is
	 * stored.
	 * 
	 * @param key
	 *            A unique identification key for this setting.
	 * @param value
	 *            The value that shall be stored.
	 * @return Returns the value that was stored at the key before overriding.
	 */
	public String put(String key, String value);

	/**
	 * Sets the value of a certain parameter in the config. When the config is
	 * written to a file the last value that was set for the <code>key</code> is
	 * stored.
	 * 
	 * If anything during encryption fails, we store the configuration value
	 * unencrypted. This is a lack of security but inevitable!
	 * 
	 * @param key
	 *            A unique identification key for this setting.
	 * @param value
	 *            The value that shall be stored.
	 * @param encrypted
	 *            If set to true, the value will be encrypted with a the salted
	 *            user password.
	 * @return Returns the value that was stored at the key before overriding.
	 */
	public String put(String key, String value, boolean encrypted);

	/**
	 * (Re)load the config
	 * 
	 * @return the current config instance
	 */
	public ICloudRAIDConfig reload();

	/**
	 * @param key
	 *            The key to remove
	 * @return Returns the value matching the removed key.
	 */
	public String remove(Object key);

	/**
	 * Writes the config to a file.
	 */
	public void save();

	/**
	 * Sets the CloudRAID user home directory path
	 * 
	 * @param path
	 *            The new path
	 */
	public void setCloudRAIDHome(String path);

	/**
	 * Sets the CloudRAID user home directory path
	 * 
	 * @param path
	 *            The new path
	 */
	public void setConfigPath(String path);
}