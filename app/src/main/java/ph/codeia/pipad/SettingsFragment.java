package ph.codeia.pipad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.SimpleChannel;


public class SettingsFragment extends PreferenceFragmentCompat {

    private Channel<Void> change;
    private boolean changed;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        findPreference(Config.HOST).setOnPreferenceChangeListener(this::showValueAsSummary);
        findPreference(Config.PORT).setOnPreferenceChangeListener(this::showValueAsSummary);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        assert root != null;
        Toolbar toolbar = (Toolbar) root.findViewById(R.id.the_toolbar);
        toolbar.setTitle(R.string.settings);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.do_launch_prefs);
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidLoaderStore store = new AndroidLoaderStore(getActivity());
        change = store.hardGet("settings-changed", SimpleChannel::new);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (changed) {
            change.send(null);
        }
    }

    boolean showValueAsSummary(Preference preference, Object value) {
        preference.setSummary(String.valueOf(value));
        changed = true;
        return true;
    }

}
