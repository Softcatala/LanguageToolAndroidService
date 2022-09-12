/*
 * Copyright (C) 2016 Jordi Mas i Hernàndez <jmas@softcatala.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.softcatala.corrector;

import android.content.Intent;
import android.preference.PreferenceActivity;


/**
 * Spell checker preference screen.
 */
public class SpellCheckerSettingsActivity extends PreferenceActivity {

    @Override
    public Intent getIntent() {
        final Intent modIntent = new Intent(super.getIntent());
        Configuration.SettingsActivity = this;
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, SpellCheckerSettingsFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SpellCheckerSettingsFragment.class.getName().equals(fragmentName);
    }
}
