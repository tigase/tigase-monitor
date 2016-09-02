/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2016 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.monitor.util;

import java.io.File;
import java.util.Arrays;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;

/**
 * Created: Sep 10, 2009 6:49:24 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 4 $
 */
public class MFileChooser extends JFileChooser {

	public MFileChooser(final String descr, final String... exts) {
		super();
		if (exts != null && exts.length > 0) {
			setName("untitled." + Arrays.toString(exts));
		} else {
			setName("new_directory");
		}
		setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				if (exts != null && exts.length > 0) {
					for (String ext : exts) {
						if (f.getName().endsWith(ext)) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public String getDescription() {
				return descr;
			}

		});
	}

}
