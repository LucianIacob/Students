package com.studios.lucian.students.model;

/**
 * Created with Love by Lucian and Pi on 03.03.2016.
 */
public class Student {
    private String matricol;
    private String groupNumber;
    private String name;
    private String surname;

    @Override
    public String toString() {
        return name + " " + surname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Student student = (Student) o;

        if (matricol != null ? !matricol.equals(student.matricol) : student.matricol != null)
            return false;
        if (groupNumber != null ? !groupNumber.equals(student.groupNumber) : student.groupNumber != null)
            return false;
        if (surname != null ? !surname.equals(student.surname) : student.surname != null)
            return false;
        return name != null ? name.equals(student.name) : student.name == null;

    }

    @Override
    public int hashCode() {
        int result = matricol != null ? matricol.hashCode() : 0;
        result = 31 * result + (groupNumber != null ? groupNumber.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public void setMatricol(String matricol) {
        this.matricol = matricol;
    }

    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMatricol() {
        return matricol;
    }

    public String getGroupNumber() {
        return groupNumber;
    }

    public String getSurname() {
        return surname;
    }

    public String getName() {
        return name;
    }

    public Student(String groupNumber, String matricol, String name, String surname) {
        this.matricol = matricol;
        this.groupNumber = groupNumber;
        this.name = name;
        this.surname = surname;
    }
}
