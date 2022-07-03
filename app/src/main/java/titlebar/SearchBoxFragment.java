package titlebar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.java.mashihe.R;

import util.ScreenUtil;

public class SearchBoxFragment extends Fragment {
    private ImageView mIvBack, mIvSearch;
    private EditText mEtSearch;
    private TitleFragment titleFragment;
    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_searchbox, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEtSearch = view.findViewById(R.id.et_search);
        mIvBack = view.findViewById(R.id.iv_back);
        mIvSearch = view.findViewById(R.id.iv_search);

        mEtSearch.setWidth(ScreenUtil.getScreenWidth(getContext())-100);

        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
