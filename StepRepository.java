package black.com.myapplication.Repositories.Repository;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import black.com.myapplication.Repositories.DAObj.stepDao;
import black.com.myapplication.Repositories.DataBase.StepDatabase;
import black.com.myapplication.Repositories.POJO.LocationStartStop;
import black.com.myapplication.Repositories.POJO.StatusCount;
import black.com.myapplication.Repositories.POJO.StepCatCount;
import black.com.myapplication.Repositories.Tables.StepTables;

public class StepRepository implements AsyncResult{
    private stepDao mstepDao;
    private MutableLiveData<List<StepTables>> searchResults =
            new MutableLiveData<>();
    private LiveData<List<StepTables>> allSteps;

    public StepRepository(Application application){
        StepDatabase stepDatabase =StepDatabase.getInstance(application);
        mstepDao =stepDatabase.stepDao();
        allSteps=mstepDao.getAllStep();
    }

    @Override
    public void asyncFinished(List<StepTables> results) {
        searchResults.setValue(results);
    }


    //insert step
    public void insert(StepTables stepTables){
         new InsertStepAsyncTask(mstepDao).execute(stepTables);
     }
     //update from notcomplete to completed
    public void updateStatus(int id){
        new UpdateStatusAsyncTask(mstepDao).execute(id);
    }
    //update from stop to start vise-versa
    public void updateStopStartLoc(LocationStartStop locationStartStop){
        new UpdateStopStartLocAsyncTask(mstepDao).execute(locationStartStop);
    }
    //update All from  stop to start vise-versa
    public void updateAllStopStartLoc(String locationStartStop){
        new UpdateAllStopStartLocAsyncTask(mstepDao).execute(locationStartStop);
    }
    //delete all step
    public void deleteAll(){
        new DeleteAllAsyncTask(mstepDao).execute();
    }
    //delete step
    public  void deleteStep(int Id){
        new DeleteStepAsyncTask(mstepDao).execute(Id);
    }
    //delete
    public void delete(StepTables stepTables){
        new DeleteAsyncTask(mstepDao).execute(stepTables);
    }

    //select by category////call select where category=? in dao
    public void getCategoryStep(String stepCat){
        SelectStepCatAsyncTask task = new SelectStepCatAsyncTask(mstepDao);
        task.delegate = this;
        task.execute(stepCat);
    }
    //select by Id////call select where category=? in dao
    public StepTables getStepById(int stepId) throws ExecutionException, InterruptedException {
        SelectStepIdAsyncTask task = new SelectStepIdAsyncTask(mstepDao);
        return  task.execute(stepId).get();
    }
    //get count
    public String getAllCountSteps() throws ExecutionException, InterruptedException {

         SelectStepCountAsyncTask task = new SelectStepCountAsyncTask(mstepDao);

         return  task.execute().get();
    }
    //get count group by status
    public List<StatusCount> getStatusCountSteps() throws ExecutionException, InterruptedException {
        List<StatusCount> listStatus=null;
        SelectStepStatusCountAsyncTask task = new SelectStepStatusCountAsyncTask(mstepDao);

        listStatus=  task.execute().get();
        return listStatus;
    }
    //get count group by categeory
    public List<StepCatCount> getCategoryCountSteps() throws ExecutionException, InterruptedException {
        List<StepCatCount> listStatus=null;
        SelectStepCategoryCountAsyncTask task = new SelectStepCategoryCountAsyncTask(mstepDao);

        listStatus=  task.execute().get();
        return listStatus;
    }
    //select by status  //call select where status=? in dao
    public List<StepTables> getStatusStep(String status) throws ExecutionException, InterruptedException {
        List<StepTables> listStatus=null;//delete if code crash
        SelectStepStatusAsyncTask task = new SelectStepStatusAsyncTask(mstepDao);
        task.delegate = this;
        listStatus=task.execute(status).get();//remove get if code crash
        return listStatus;//also remove dont return anything
        //find other way to do it
    }

    //Select by status and place
    public List<StepTables> getStatusPlaceStep(String status) throws ExecutionException, InterruptedException {
        List<StepTables> listStatusPlace=null;//delete if code crash
        SelectStepStatusPlaceAsyncTask task = new SelectStepStatusPlaceAsyncTask(mstepDao);
        task.delegate = this;
        listStatusPlace=task.execute(status).get();//remove get if code crash
        return listStatusPlace;//also remove dont return anything
        //find other way to do it
    }
    //select step by place!=null, notcomplete and stepStopStartLoc=start
    public List<StepTables> getStopStaartLocStep(String status) throws ExecutionException, InterruptedException {
        List<StepTables> listStopStartLoc=null;//delete if code crash
        SelectStepStopStartLocAsyncTask task = new SelectStepStopStartLocAsyncTask(mstepDao);
        task.delegate = this;
        listStopStartLoc=task.execute(status).get();//remove get if code crash
        return listStopStartLoc;//also remove dont return anything
        //find other way to do it
    }

    public List<StepTables> getSearchStep(String status) throws ExecutionException, InterruptedException {
        List<StepTables> listStatus=null;//delete if code crash
        SelectStepSearchAsyncTask task = new SelectStepSearchAsyncTask(mstepDao);
        task.delegate = this;
        listStatus=task.execute(status).get();//remove get if code crash
        return listStatus;//also remove dont return anything
        //find other way to do it
    }

    //get all step //call select * in dao
    public LiveData<List<StepTables>> getAllSteps() {
        return allSteps;
    }
    public MutableLiveData<List<StepTables>> getSearchResults() {
        return searchResults;
    }


    //do in background insert
    private static class InsertStepAsyncTask extends AsyncTask<StepTables,Void,Void>{
        private stepDao mstepDao;

        private InsertStepAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }

        @Override
        protected Void doInBackground(StepTables... stepTables) {
            mstepDao.insert(stepTables[0]);
            return null;
        }
    }
    //do in background delete
    private static class DeleteAsyncTask extends AsyncTask<StepTables,Void,Void>{
        private stepDao mstepDao;

        private DeleteAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }

        @Override
        protected Void doInBackground(StepTables... stepTables) {
            mstepDao.delete(stepTables[0]);
            return null;
        }
    }
    //do in background select by id
    private static class SelectStepIdAsyncTask extends AsyncTask<Integer,Void,StepTables>{
        private stepDao mstepDao;
        //private StepRepository delegate = null;
        private SelectStepIdAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }


        @Override
        protected StepTables doInBackground(Integer... integers) {
            return  mstepDao.getStepById(integers[0]);

        }

    }
   //delete all in background
    private static class DeleteAllAsyncTask extends AsyncTask<Void,Void,Void>{
        private stepDao mstepDao;

        private DeleteAllAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mstepDao.deleteAll();
            return null;
        }
    }
    //delete step in background
    private static class DeleteStepAsyncTask extends AsyncTask<Integer,Void,Void>{
        private stepDao mstepDao;

        private DeleteStepAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            mstepDao.deleteStep(integers[0]);
            return null;
        }
    }
    //select category in background
    private static class SelectStepCatAsyncTask extends AsyncTask<String,Void,List<StepTables>>{
        private stepDao mstepDao;
        private StepRepository delegate = null;
        private SelectStepCatAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}
        @Override
        protected List<StepTables> doInBackground(String... strings) {
           return mstepDao.getCategoryStep(strings[0]);
        }
        @Override
        protected void onPostExecute(List<StepTables> result) {
            delegate.asyncFinished(result);
        }
    }
    //select status in background
    private static class SelectStepStatusAsyncTask extends AsyncTask<String,Void,List<StepTables>>{
        private stepDao mstepDao;
        private StepRepository delegate = null;
        private SelectStepStatusAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}
        @Override
        protected List<StepTables> doInBackground(String... strings) {
            return mstepDao.getStatusStep(strings[0]);
        }
        @Override
        protected void onPostExecute(List<StepTables> result) {
            delegate.asyncFinished(result);
        }
    }
    //select status in background
    private static class SelectStepStatusPlaceAsyncTask extends AsyncTask<String,Void,List<StepTables>>{
        private stepDao mstepDao;
        private StepRepository delegate = null;
        private SelectStepStatusPlaceAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}
        @Override
        protected List<StepTables> doInBackground(String... strings) {
            return mstepDao.getStatusPlaceStep(strings[0],"NoPlaces");
        }
        @Override
        protected void onPostExecute(List<StepTables> result) {
            delegate.asyncFinished(result);
        }
    }
    //select stopstart in background
    private static class SelectStepStopStartLocAsyncTask extends AsyncTask<String,Void,List<StepTables>>{
        private stepDao mstepDao;
        private StepRepository delegate = null;
        private SelectStepStopStartLocAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}
        @Override
        protected List<StepTables> doInBackground(String... strings) {
            return mstepDao.getStopStartLocStep(strings[0],"NoPlaces","Start");
        }
        @Override
        protected void onPostExecute(List<StepTables> result) {
            delegate.asyncFinished(result);
        }
    }
    //select search
    private class SelectStepSearchAsyncTask extends AsyncTask<String,Void,List<StepTables>>{
        //for search result
        private stepDao mstepDao;
        private StepRepository delegate = null;
        private SelectStepSearchAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}
        @Override
        protected List<StepTables> doInBackground(String... strings) {
            return mstepDao.getSearchStep(strings[0]);
        }
        @Override
        protected void onPostExecute(List<StepTables> result) {
            delegate.asyncFinished(result);
        }
    }
    //count
    private class SelectStepCountAsyncTask extends AsyncTask<Void,Void,String>{

        private stepDao mstepDao;
        private SelectStepCountAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}

        @Override
        protected String doInBackground(Void... voids) {

            return String.valueOf(mstepDao.getAllStepCount());
        }

    }
    //count
    private class SelectStepStatusCountAsyncTask extends AsyncTask<Void,Void,List<StatusCount>>{

        private stepDao mstepDao;
        private SelectStepStatusCountAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}

        @Override
        protected List<StatusCount> doInBackground(Void... voids) {

            return mstepDao.getStatusStepCount();
        }

    }

    //count StepCat
    //count
    private class SelectStepCategoryCountAsyncTask extends AsyncTask<Void,Void,List<StepCatCount>>{

        private stepDao mstepDao;
        private SelectStepCategoryCountAsyncTask(stepDao mstepDao){this.mstepDao=mstepDao;}

        @Override
        protected List<StepCatCount> doInBackground(Void... voids) {

            return mstepDao.getCategoryStepCount();
        }

    }

    //update status Async in background
    private static class UpdateStatusAsyncTask extends AsyncTask<Integer,Void,Void>{
        private stepDao mstepDao;

        private UpdateStatusAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            mstepDao.markASCompleted("Completed",integers[0]);
            return null;
        }
    }
    //update status Async in background
    private static class UpdateStopStartLocAsyncTask extends AsyncTask<LocationStartStop,Void,Void>{
        private stepDao mstepDao;

        private UpdateStopStartLocAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }


        @Override
        protected Void doInBackground(LocationStartStop... locationStartStops) {
            mstepDao.updateStartLocService(locationStartStops[0].getStartOrStop(),locationStartStops[0].getId());
            return null;
        }
    }
    //update all step to  Async in background
    private static class UpdateAllStopStartLocAsyncTask extends AsyncTask<String,Void,Void>{
        private stepDao mstepDao;

        private UpdateAllStopStartLocAsyncTask(stepDao mstepDao){
            this.mstepDao=mstepDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            mstepDao.updateAllStartLoc(strings[0]);
            return null;
        }
    }

}
