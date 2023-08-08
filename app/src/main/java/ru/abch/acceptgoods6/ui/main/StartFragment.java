package ru.abch.acceptgoods6.ui.main;

import android.annotation.SuppressLint;
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
import ru.abch.acceptgoods6.ScannedCode;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {
    EditText etStoreman, etCamera;
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        etStoreman = view.findViewById(R.id.et_storeman);
        tvStore = view.findViewById(R.id.tv_store);
        etCamera = view.findViewById(R.id.et_camera);
        if (App.warehouse != null) tvStore.setText(App.warehouse.descr.trim());
//        btStart1 = view.findViewById(R.id.bt_start1);

        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        etCamera.setEnabled(false);
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
                    etStoreman.setEnabled(false);
                    etCamera.setEnabled(true);
                    etCamera.requestFocus();
                    MainActivity.say(getResources().getString(R.string.enter_camera_tts));
                }
            }
            return false;
        });
        etStoreman.requestFocus();
        etCamera.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        (i == KeyEvent.KEYCODE_ENTER)){
                    String sCam = ((EditText) view).getText().toString();
                    int cam = 0;
                    try {
                        cam = Integer.parseInt(sCam);
                        FL.d(TAG, "Camera = " + cam);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (cam > 0) {
                        ((MainActivity) requireActivity()).getCamera(cam);
                        ((MainActivity) requireActivity()).getStoremanId();
                        ((MainActivity) requireActivity()).gotoMainFragment();
                    }
                }
                return false;
            }
        });
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
    public void setStore() {
        if(App.warehouse != null) tvStore.setText(App.getStoreName());
    }
    public void processScan(ScannedCode scannedCode) {
        if(scannedCode.codeId.equals("b") && scannedCode.data.length() > 4) {
            String sCam = scannedCode.data.substring(scannedCode.data.length()-3);
            try {
                int cam = Integer.parseInt(sCam);
                FL.d(TAG, "Camera = " + cam);
                if(cam > 0 && App.getStoreMan() > 0) {
                    ((MainActivity) requireActivity()).getCamera(cam);
                    ((MainActivity) requireActivity()).getStoremanId();
                    ((MainActivity) requireActivity()).gotoMainFragment();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}