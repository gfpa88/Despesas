package pt.gon.expensivessheet.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import pt.gon.expensivessheet.databinding.FragmentAnalyticsBinding;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
/*
        activity = getActivity();

        id = getActivity().getIntent().getExtras().getString("id");
                mAdapter = new SimpleStringAdapter(this, mlist);

        FloatingActionButton fabExpand = binding.fabExpand;
        FloatingActionButton fabSearch = binding.fabSearch;
        FloatingActionButton fab = binding.fab;*/


        return root;
    }
}