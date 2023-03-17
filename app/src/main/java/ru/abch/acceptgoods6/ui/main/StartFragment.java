package ru.abch.acceptgoods6.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bosphere.filelogger.FL;

import java.util.ArrayList;

import ru.abch.acceptgoods6.App;
import ru.abch.acceptgoods6.MainActivity;
import ru.abch.acceptgoods6.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {
    EditText etStoreman;
    TextView tvStore;
    Button btStart1;
    static int storeMan = -1;
    private static final String TAG = "StartFragment";
    String sStoreMan;

    public StartFragment() {
        // Required empty public constructor
    }
    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        etStoreman = view.findViewById(R.id.et_storeman);
        tvStore = view.findViewById(R.id.tv_store);
        if (App.getStoreIndex() > 0) tvStore.setText(((MainActivity)getActivity()).names[App.getStoreIndex()]);
//        btStart1 = view.findViewById(R.id.bt_start1);

        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        etStoreman.setText(String.valueOf(App.getStoreMan()));
        etStoreman.setOnKeyListener((view, i, keyEvent) -> {
            if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                    (i == KeyEvent.KEYCODE_ENTER)){
                sStoreMan = ((EditText) view).getText().toString();
                try {
                    storeMan = Integer.parseInt(sStoreMan);
                    FL.d(TAG, "Storeman = " + storeMan);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (storeMan > 0) {
                    App.setStoreMan(storeMan);
                    ((MainActivity) getActivity()).gotoMainFragment();
                }
            }
            return false;
        });
        etStoreman.requestFocus();
/*
        btStart1.setOnClickListener(view -> {
            sStoreMan = etStoreman.getText().toString();
                try {
                    storeMan = Integer.parseInt(sStoreMan);
                    FL.d(TAG, "Storeman = " + storeMan);
                    App.setStoreMan(storeMan);
                    ((MainActivity) getActivity()).gotoMainFragment();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    MainActivity.say(getResources().getString(R.string.storeman_number_tts));
                }
        });

 */
    }
}