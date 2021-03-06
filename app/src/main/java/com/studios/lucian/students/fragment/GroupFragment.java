package com.studios.lucian.students.fragment;

import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.studios.lucian.students.R;
import com.studios.lucian.students.activity.DisplayStudentActivity;
import com.studios.lucian.students.activity.MainActivity;
import com.studios.lucian.students.adapter.StudentsListAdapter;
import com.studios.lucian.students.model.Grade;
import com.studios.lucian.students.model.Group;
import com.studios.lucian.students.model.Presence;
import com.studios.lucian.students.model.Student;
import com.studios.lucian.students.repository.GradesDAO;
import com.studios.lucian.students.repository.GroupDAO;
import com.studios.lucian.students.repository.PresenceDAO;
import com.studios.lucian.students.sync.DriveSyncHandler;
import com.studios.lucian.students.util.DialogsHandler;
import com.studios.lucian.students.util.StudentButtonsListener;
import com.studios.lucian.students.util.StudentsDbHandler;
import com.studios.lucian.students.util.Validator;
import com.studios.lucian.students.util.listener.StudentActionsListener;

import java.util.List;

public class GroupFragment extends ListFragment
        implements AdapterView.OnItemClickListener, StudentActionsListener, StudentButtonsListener {

    private static final String TAG = GroupFragment.class.getSimpleName();
    private static final String GROUP = "Group ";
    private static final String KEY_MATRICOL = "matricol";
    private final String EMPTY_SPACE = " ";

    private FloatingActionButton mFloatingActionButton;
    private TextView emptyText;
    private DriveSyncHandler mDriveSyncHandler;
    private StudentsListAdapter listAdapter;
    private StudentsDbHandler mStudentsDbHandler;
    private DialogsHandler mDialogsHandler;
    private GradesDAO mGradesDao;
    private List<Student> mStudentsList;
    private PresenceDAO mPresenceDao;
    private Group mCurrentGroup;
    private GroupDAO mGroupDao;
    private final View.OnClickListener floatingButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();
            final View alertView = inflater.inflate(R.layout.dialog_add_student, null);

            TextView dialogGroupNumber = (TextView) alertView.findViewById(R.id.dialog_group_number);
            final EditText dialogMatricol = (EditText) alertView.findViewById(R.id.dialog_student_id);
            final EditText dialogName = (EditText) alertView.findViewById(R.id.dialog_name);
            final EditText dialogSurname = (EditText) alertView.findViewById(R.id.dialog_surname);
            dialogGroupNumber.setText(getDialogGroupNumberText());

            dialog.setView(alertView)
                    .setPositiveButton(R.string.button_add_student, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean inputsAreOk = true;
                            if (!Validator.isValidMatricol(dialogMatricol.getText().toString())) {
                                inputsAreOk = false;
                                dialogMatricol.setError("Invalid Student ID");
                            }
                            if (!Validator.isValidName(dialogName.getText().toString())) {
                                inputsAreOk = false;
                                dialogName.setError("Invalid Name");
                            }
                            if (!Validator.isValidName(dialogSurname.getText().toString())) {
                                inputsAreOk = false;
                                dialogSurname.setError("Invalid Surname");
                            }
                            if (inputsAreOk) {
                                addNewStudent(
                                        dialogMatricol.getText().toString(),
                                        dialogName.getText().toString(),
                                        dialogSurname.getText().toString());
                            } else {
                                showWarning(R.string.error_add_student_title, R.string.error_add_student_message);
                            }
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    };

    public GroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        GroupDAO groupDAO = new GroupDAO(getActivity());

        String groupNumber = bundle.getString("groupNumber");
        mCurrentGroup = groupDAO.get(groupNumber);

        mStudentsDbHandler = new StudentsDbHandler(getActivity());
        mGroupDao = new GroupDAO(getActivity());
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyText = (TextView) view.findViewById(android.R.id.empty);
        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(floatingButtonClick);
        getListView().setOnItemClickListener(this);
        mDialogsHandler = new DialogsHandler(getActivity(), this);
        this.mGradesDao = new GradesDAO(getActivity());
        this.mPresenceDao = new PresenceDAO(getActivity());
        MainFragment mainFragment = ((MainActivity) getActivity()).getMainFragment();
        mDriveSyncHandler = new DriveSyncHandler(mainFragment.getGoogleApiClient(), null);
        setAdapter();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setEmptyView(emptyText);
        getActivity().setTitle(GROUP + mCurrentGroup.getNumber());
    }

    @Override
    public void onResume() {
        super.onResume();
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            AdapterView.AdapterContextMenuInfo info =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.student_menu_edit:
                    editStudent((int) info.id);
                    return true;
                case R.id.student_menu_delete:
                    deleteStudent((int) info.id);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }
        return super.onContextItemSelected(item);
    }

    private void addNewStudent(String matricol, String name, String surname) {
        Student student = new Student(mCurrentGroup.getNumber(), matricol, name, surname);
        if (mStudentsDbHandler.addStudent(student)) {
            mGroupDao.increaseCount(mCurrentGroup);
            ((MainActivity) getActivity()).getMainFragment().syncGroups();
            mStudentsList.add(student);
            listAdapter.notifyDataSetChanged();
            Toast.makeText(getActivity(), R.string.student_added, Toast.LENGTH_SHORT).show();
        } else {
            showWarning(R.string.student_id_duplicated_title, R.string.student_id_duplicated_message);
        }
    }

    private void editStudent(final int id) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View alertView = inflater.inflate(R.layout.dialog_edit_student, null);

        TextView dialogStudentName = (TextView) alertView.findViewById(R.id.dialog_student_name);
        final TextView dialogMatricolNumber = (TextView) alertView.findViewById(R.id.dialog_matricol_number);
        final EditText dialogName = (EditText) alertView.findViewById(R.id.dialog_edit_name);
        final EditText dialogSurname = (EditText) alertView.findViewById(R.id.dialog_edit_surname);

        final Student student = mStudentsList.get(id);

        dialogStudentName.setText(student.toString());
        dialogMatricolNumber.setText(student.getMatricol());

        dialog.setView(alertView)
                .setPositiveButton(R.string.button_update_student, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean inputsAreOk = true;
                        if (!Validator.isValidName(dialogName.getText().toString())) {
                            inputsAreOk = false;
                            dialogName.setError("Invalid Name");
                        }
                        if (!Validator.isValidName(dialogSurname.getText().toString())) {
                            inputsAreOk = false;
                            dialogSurname.setError("Invalid Surname");
                        }
                        if (inputsAreOk) {
                            updateStudent(
                                    id,
                                    dialogMatricolNumber.getText().toString(),
                                    dialogName.getText().toString(),
                                    dialogSurname.getText().toString());
                        } else {
                            showWarning(R.string.error_update_student_title, R.string.error_update_student_message);
                        }
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void updateStudent(int id, String matricol, String name, String surname) {
        Student student = new Student(mCurrentGroup.getNumber(), matricol, name, surname);
        if (mStudentsDbHandler.updateStudent(student)) {
            mStudentsList.set(id, student);
            listAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), R.string.error_update_student, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStudent(int id) {
        Student student = mStudentsList.get(id);
        if (mStudentsDbHandler.deleteStudent(student)) {
            mGroupDao.decreaseCount(mCurrentGroup);
            ((MainActivity) getActivity()).getMainFragment().syncGroups();
            mStudentsList.remove(id);
            listAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getActivity(), R.string.student_not_found, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void setAdapter() {
        mStudentsList = mStudentsDbHandler.getStudentsFromGroup(mCurrentGroup.getNumber());
        listAdapter = new StudentsListAdapter(getActivity(), mStudentsList, this);
        setListAdapter(listAdapter);
    }

    private void showWarning(int errorTitle, int errorMessage) {
        new AlertDialog
                .Builder(getActivity())
                .setTitle(errorTitle)
                .setMessage(errorMessage)
                .setPositiveButton(R.string.OK, null)
                .create()
                .show();
    }

    @Override
    public void onPause() {
        unregisterForContextMenu(getListView());
        super.onPause();
    }

    private String getDialogGroupNumberText() {
        return getString(R.string.dialog_group_number) + EMPTY_SPACE + mCurrentGroup.getNumber();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Student student = (Student) getListView().getItemAtPosition(i);
        Intent intent = new Intent(getActivity(), DisplayStudentActivity.class);
        intent.putExtra(KEY_MATRICOL, student.getMatricol());
        startActivity(intent);
    }

    @Override
    public void onPresenceClick(Student student) {
        mDialogsHandler.showAddPresenceDialog(student);
    }

    @Override
    public void onGradeClick(Student student) {
        mDialogsHandler.showAddGradeDialog(student);
    }

    @Override
    public void addGrade(Student student, Grade grade) {
        boolean addedSuccessfully = mGradesDao.add(grade);
        if (addedSuccessfully) {
            if (mDriveSyncHandler.syncGrade(grade, mCurrentGroup, student)) {
                Toast.makeText(getActivity(), grade.getToastDisplay(student), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void addPresence(Student student, Presence presence) {
        boolean addedSuccessfully = mPresenceDao.add(presence);
        if (addedSuccessfully) {
            if (mDriveSyncHandler.syncPresence(presence, mCurrentGroup, student)) {
                Toast.makeText(getActivity(), presence.getToastDisplay(student), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
            }
        }
    }
}