# Synchronization
## Introducing
Synchronization is an Android Library for synchronize between Android devices on a local Wi-Fi network.
## Add Synchronization your project
To add Synchronization to your project, at first, add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
   }
}
```
Second, add the dependency
```
implementation 'com.github.duccnv-1684:Synchronization:1.0.0'
```
And you're all set!
## Use Synchronization
### Create instance to of SynchronizationAlgorithm
To use Synchronization, you have to create an instance of SynchronizationAlgorithm class:

```
        SynchronizationAlgorithm synchronizationAlgorithm = new SynchronizationAlgorithm.Builder()
                .setContext(context)
                .setId(id)
                .setLooper(looper)
                .setListener(new SynchronizationAlgorithm.OnSynchronizationEventListener() {
                    @Override
                    public void onRequestAccepted() {
                        //Do something when your request accepted!
                    }
                })
                .setSynchronizationAlgorithmType(type)
                .build();
```
You can create an instance via SynchronizationAlgorithm's Builder. You have to pass an Android Context, an ID, an Looper, an callback for recall when your request accepted and you can choose an Synchronization Algorithm.
In this library, there're three Synchronization Algorithm was implemented: Centralized, Distributed and Token Ring.
You have to you enum class SynchronizationAlgorithmType to select. Example: `SynchronizationAlgorithmType.CENTRALIZED_ALGORITHM`
### Start synchronize
When you've created an insance of SynchronizationAlgorithm, we can call `startSynchronize()` method to start synchronizing.
### Request access to shared resource
After an instance of SynchronizationAlgorithm was created, if you want to access to shared resources, we can call `requestAccess()` to send an request and sit back to wait for `onRequestAccepted() is fire. At this point, you can access to shared resource.
If you want to abort request, just call `cancelRequest()` method.
### Stop synchronize
When you finish the synchronization process, call `stopSynchronize()` to stop.
