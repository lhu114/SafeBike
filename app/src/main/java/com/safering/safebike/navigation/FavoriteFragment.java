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
         *  ��Ʈ��ũ ��û �̸��� ������ ������ ���ã�� �׸� ��������
         */

        listView.setAdapter(mAdapter);

        initData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                 *  �ش� �����ۿ� ������ �ȳ����� ȭ������ �ѱ�
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
//        builder.setTitle("��ü �׸� ����");
                builder.setMessage("��ü �׸��� �����Ͻðڽ��ϱ�");
                builder.setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         * ������ �̸��� delete all ��ɾ� ������ ���ã�� ��� �� ����
                         * adapter ���� (adpater clear)
                         *
                         * Visibility Gone ó��
                         */

                        mAdapter.notifyDataSetChanged();
                        mAdapter.notifyDataSetInvalidated();
                    }
                });
                builder.setNegativeButton("���", new DialogInterface.OnClickListener() {
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
         * ��Ʈ��ũ ��û�ؼ� ���ã�� ������ ��������
         */

        for (int i = 0; i < 10; i++) {
            FavoriteItem itemData = new FavoriteItem();
            itemData.fvPOIName = "���ã�� ������";
            mAdapter.add(itemData);
        }
    }
}