package titlebar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.java.mashihe.MainActivity;
import com.java.mashihe.R;

import util.ScreenUtil;

public class TitleFragment extends Fragment {
    private ImageView mIvMenu, mIvSearch;
    private TextView mTvTitle;
    private SearchBoxFragment searchBoxFragment;
    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_title, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle = view.findViewById(R.id.tv_title);
        mIvMenu = view.findViewById(R.id.iv_menu);
        mIvSearch = view.findViewById(R.id.iv_search);

        mTvTitle.setWidth(ScreenUtil.getScreenWidth(getContext())-100);
        fragmentManager = getFragmentManager();

        mIvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity)getActivity();
                mainActivity.showMenu();
            }
        });

        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity)getActivity();
                mainActivity.showSearchBox();
            }
        });

        //搜索框通过另一个fragment实现
//        mIvSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (searchBoxFragment == null) {
//                    searchBoxFragment = new SearchBoxFragment();
//                }
//                Fragment fragment = fragmentManager.findFragmentByTag("title");
//                FragmentTransaction fTransaction = fragmentManager.beginTransaction();
//                fTransaction.setCustomAnimations(R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit, R.animator.fragment_slide_left_enter, R.animator.fragment_slide_left_exit);
//                if (fragment!=null) {
//                    fTransaction.hide(fragment).add(R.id.fl_title, searchBoxFragment).addToBackStack(null).commitAllowingStateLoss();
//                } else {
//                    fTransaction.replace(R.id.fl_title, searchBoxFragment).addToBackStack(null).commitAllowingStateLoss();
//                }
//            }
//        });


    }
}
