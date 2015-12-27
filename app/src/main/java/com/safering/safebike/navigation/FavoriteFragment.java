package com.safering.safebike.navigation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.R;
import com.safering.safebike.manager.FontManager;
import com.safering.safebike.manager.NetworkManager;
import com.safering.safebike.property.PropertyManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {
    ListView listView;
    FavoriteAdapter mAdapter;
    TextView messageView;
    View deleteBtn;

    private static final int SUCCESS = 200;

    public FavoriteFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        messageView = (TextView) view.findViewById(R.id.text_message_favorite);
       // deleteBtn = (ImageButton) view.findViewById(R.id.btn_delete_favorite);
        listView = (ListView) view.findViewById(R.id.listView_favorite);
        mAdapter = new FavoriteAdapter();

        /*
         *  네트워크 요청 이메일 서버에 보내서 즐겨찾기 항목 가져오기
         */

        deleteBtn = inflater.inflate(R.layout.delete_favorite_view, null);
        listView.addFooterView(deleteBtn);
        listView.setAdapter(mAdapter);

        initData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                 *  해당 아이템에 목적지 안내시작 화면으로 넘김
                 */
                FavoriteItem fvItem = (FavoriteItem) listView.getItemAtPosition(position);

                if (getActivity() != null) {
                    ((ParentRctFvActivity) getActivity()).sendFavoritePOI(fvItem);
                }
            }
        });

        deleteBtn.findViewById(R.id.btn_delete_favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setIcon(android.R.drawable.ic_dialog_info);
//        builder.setTitle("전체 항목 삭제");
                builder.setMessage("전체 항목을 삭제하시겠습니까");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         * 서버로 이메일 delete all 명령어 보내서 즐겨찾기 목록 다 지움
                         * adapter 갱신 (adpater clear)
                         *
                         * Visibility Gone 처리
                         */
//                        Log.d("safebike", "FavoriteFragment.initData");

                        final String userEmail = PropertyManager.getInstance().getUserEmail();

                        NetworkManager.getInstance().removeAllFavorite(getContext(), userEmail, new NetworkManager.OnResultListener() {
                            @Override
                            public void onSuccess(Object result) {
//                                Log.d("safebike", "FavoriteFragment.removeAllFavorite.onSuccess");

                                if ((int) result == SUCCESS) {
//                                    Log.d("safebike", "FavoriteFragment.removeAllFavorite.onSuccess.200");

                                    mAdapter.remove();

                                    messageView.setVisibility(View.VISIBLE);
                                    listView.setVisibility(View.GONE);
                                    deleteBtn.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onFail(int code) {
//                                Log.d("safebike", "FavoriteFragment.removeAllFavorite.onFail");
                            }
                        });

                        /*
                         *  remove 에서 했는데 관련 여부 따져서 삭제
                         */
//                        mAdapter.notifyDataSetChanged();
//                        mAdapter.notifyDataSetInvalidated();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
//        builder.setCancelable(false);

                builder.create().show();
            }
        });

        messageView.setTypeface(FontManager.getInstance().getTypeface(getContext(), FontManager.NOTOSANS));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initData() {
        /*
         * 네트워크 요청해서 즐겨찾기 데이터 가져오기
         */
//        Log.d("safebike", "FavoriteFragment.initData");

        final String userEmail = PropertyManager.getInstance().getUserEmail();

        NetworkManager.getInstance().getFavorite(getContext(), userEmail, new NetworkManager.OnResultListener<FavoriteResult>() {
            @Override
            public void onSuccess(FavoriteResult result) {
                if (result.favoriteItemList != null) {
                    if (result.favoriteItemList.size() > 0) {
//                        Log.d("safebike", "FavoriteFragment.initData.onSuccess.favoriteItemList.size : " + Integer.toString(result.favoriteItemList.size()));

                        for (FavoriteItem item : result.favoriteItemList) {
                            mAdapter.add(item);
                        }
                    }
                }

                if (mAdapter.getCount() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                } else {
                    messageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFail(int code) {
//                Log.d("safebike", "FavoriteFragment.initData.onFail");

                messageView.setVisibility(View.VISIBLE);
            }
        });
    }
}