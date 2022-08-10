package pt.gon.expensivessheet.ui;

import androidx.fragment.app.Fragment;

public abstract class SheetFragment extends Fragment {

    public abstract void delete(int index);
    public abstract void edit(int index, String value);
}
