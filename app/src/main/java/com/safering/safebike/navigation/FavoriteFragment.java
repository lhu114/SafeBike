package com.safering.safebike.navigation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment {
    ListView listView;
    FavoriteAdapter mAdapter;
    TextView messageView;
    Button deleteBtn;

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
        deleteBtn = (Button) view.findViewById(R.id.btn_delete_favorite);
        listView = (ListView) view.findViewById(R.id.listView_favorite);
        mAdapter = new FavoriteAdapter();

        /*
         *  네트워크 요청 이메일 서버에 보내서 즐겨찾기 항목 가져오기
         */

        listView.setAdapter(mAdapter);

        initData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                 *  해당 아이템에 목적지 안내시작 화면으로 넘김
                 */
                Intent intent = new Intent(getContext(), SelectRouteActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
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

                        mAdapter.notifyDataSetChanged();
                        mAdapter.notifyDataSetInvalidated();
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

        if (mAdapter.getCount() > 0) {
            messageView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        } else {

        }

        return view;
    }

    private void initData() {
        /*
         * 네트워크 요청해서 즐겨찾기 데이터 가져오기
         */

        for (int i = 0; i < 10; i++) {
            FavoriteItem itemData = new FavoriteItem();
            itemData.fvPOIName = "즐겨찾기 목적지";
            mAdapter.add(itemData);
        }
    }
}