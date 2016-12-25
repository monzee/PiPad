package ph.codeia.pipad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ph.codeia.androidutils.AndroidLoaderStore;
import ph.codeia.signal.Channel;
import ph.codeia.signal.SimpleChannel;


public class SettingsFragment extends PreferenceFragmentCompat {

    private Channel<Void> changed;
    private boolean didChange;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
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
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidLoaderStore store = new AndroidLoaderStore(getActivity());
        changed = store.hardGet("settings-changed", SimpleChannel::new);
        Config source = store.hardGet("config", PiPad::config);
        Preference hostPref = findPreference(Config.HOST);
        Preference portPref = findPreference(Config.PORT);
        hostPref.setOnPreferenceChangeListener(this::showValueAsSummary);
        hostPref.callChangeListener(source.host());
        portPref.setOnPreferenceChangeListener(this::showValueAsSummary);
        portPref.callChangeListener(source.port());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (didChange) {
            changed.send(null);
        }
    }

    boolean showValueAsSummary(Preference preference, Object value) {
        preference.setSummary(String.valueOf(value));
        didChange = true;
        return true;
    }

}
