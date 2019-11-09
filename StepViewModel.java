package black.com.myapplication.ViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import black.com.myapplication.Repositories.POJO.LocationStartStop;
import black.com.myapplication.Repositories.POJO.StatusCount;
import black.com.myapplication.Repositories.POJO.StepCatCount;
import black.com.myapplication.Repositories.Repository.StepRepository;
import black.com.myapplication.Repositories.Tables.StepTables;

public class StepViewModel extends AndroidViewModel {
    private StepRepository repository;
    private LiveData<List<StepTables>> allSteps;
    private MutableLiveData<List<StepTables>> searchResults;


    //for passing data between fragment and activity
    //create data to send
    private MutableLiveData<String> vmStatus;
    private MutableLiveData<String> vmSearch;
    private MutableLiveData<HashMap<String,String>> vmStepPlace;
    private MutableLiveData<List<StatusCount>> vmStepStatusCount;
    private MutableLiveData<List<StepCatCount>> vmStepCatCount;
   // private MutableLiveData<String > vmMenuData;
    public void init(){
        vmStatus=new MutableLiveData<>();
        vmSearch =new MutableLiveData<>();
        vmStepPlace =new MutableLiveData<>();
        vmStepStatusCount=new MutableLiveData<>();
        vmStepCatCount = new MutableLiveData<>();
      //  vmMenuData =new MutableLiveData<>();
    }
    public void sendStatus(String status){
        vmStatus.setValue(status);
    }
    public MutableLiveData<String> getVmStatus(){
        return vmStatus;
    }
    public void sendVmSearch(String search){
        vmSearch.setValue(search);
    }
    public MutableLiveData<String> getVmSearch(){

        return vmSearch;
    }
    public void sendSearchPlaces(HashMap<String,String> strings){
        vmStepPlace.setValue(strings);
//        vmPlaces.setValue(lat);
//        vmPlaces.setValue(lon);
    }
    public LiveData<HashMap<String, String>> getVmSearchPlaces(){

        return vmStepPlace;
    }
   //status count send from stepFragment toReport fragment
    public void sendStatusCount(List<StatusCount> statusCounts){
        vmStepStatusCount.setValue(statusCounts);
    }
    public LiveData<List<StatusCount>> getVmStatusCount(){
        return  vmStepStatusCount;
    }
    //stepCategory count send from step fragment to Report fragment
    //status count send from stepFragment toReport fragment
    public void sendCategoryCount(List<StepCatCount> categoryCounts){
        vmStepCatCount.setValue(categoryCounts);
    }
    public LiveData<List<StepCatCount>> getVmCategoryCount(){
        return  vmStepCatCount;
    }
//    public void sendMenuData(String menuData){
//        vmMenuData.setValue(menuData);
//    }
//     public MutableLiveData<String> getVmMenuData(){
//      return vmMenuData;
//     }
    public StepViewModel(@NonNull Application application) {
        super(application);
        repository = new StepRepository(application);
        allSteps = repository.getAllSteps();
        searchResults = repository.getSearchResults();
    }
    //provide search result
    public MutableLiveData<List<StepTables>> getSearchResults() {
        return searchResults;
    }

    //provide all steps
    public LiveData<List<StepTables>> getAllSteps() {
        return allSteps;
    }

    public void insertSteps(StepTables stepTables){
     repository.insert(stepTables);
    }
    public void delete(StepTables stepTables){repository.delete(stepTables);}
    public void deleteAllSteps(){
        repository.deleteAll();
    }
    public void deleteStep(int Id){
        repository.deleteStep(Id);
    }
    public void getStepByCat(String category){
        repository.getCategoryStep(category);
    }
    public List<StepTables> getStepByStatus(String status) throws ExecutionException, InterruptedException {
        return repository.getStatusStep(status);
    }
    public List<StepTables> getStepBySearch(String search) throws ExecutionException, InterruptedException {
        return repository.getSearchStep(search);
    }
    public String getAllCountStep() throws ExecutionException, InterruptedException {
        return repository.getAllCountSteps();
    }
    public List<StatusCount> getStatusCountStep() throws ExecutionException, InterruptedException {
        return repository.getStatusCountSteps();
    }
    public List<StepCatCount> getCategoryCountStep() throws ExecutionException, InterruptedException {
        return repository.getCategoryCountSteps();
    }
    public boolean updateStatus(int stepId){
        repository.updateStatus(stepId);
        return true;
    }
    public void updateStartStopLoc(LocationStartStop locationStartStop){
        repository.updateStopStartLoc(locationStartStop);
    }
    public boolean updateAllStartStopLoc(){
        repository.updateAllStopStartLoc("Stop");
        return true;
    }
    public List<StepTables> getStepByStatusPlaces(String status) throws ExecutionException, InterruptedException {
        return repository.getStatusPlaceStep(status);
    }
    public List<StepTables> getStepStopStartLoc(String status) throws ExecutionException, InterruptedException {
        return repository.getStopStaartLocStep(status);
    }
    public StepTables getStepById(int stepId) throws ExecutionException, InterruptedException {
        return repository.getStepById(stepId);
    }
}
