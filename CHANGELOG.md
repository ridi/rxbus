# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.1.0] - 2018-09-03
### Removed
- `RxBusAndroid` was removed.
  - Use other tools instead such as [AutoDispose](https://github.com/uber/AutoDispose) or manage subscriptions manually.

## [1.0.2] - 2018-06-22
### Fixed
- Avoid `ConcurrentModificationException` on loop in `RxBus.post`.

## [1.0.1] - 2018-05-21
### Added
- [RxBusAndroid] Add `android.support.v4.app.Fragment.rxBusObservable`.

### Changed
- Upagrade base `RxJava` library version from 2.0.7 to 2.1.3.
- Check event object's identity instead of equality when removing a sticky event.
- [RxBusAndroid] Add a dependency to `rxlifecycle-kotlin`.

## [1.0.0] - 2017-05-22

**The first stable release! :tada:**

## [1.0.0-rc4] - 2017-04-14
### Fixed
- [RxBusAndroid] Check if the view is null when fragment binding.

## [1.0.0-rc3] - 2017-03-29
### Fixed
- Fix a bug that sticky event was not delivered to its super classes’ subscribers.

## [1.0.0-rc2] - 2017-03-13
### Added
- [RxBusAndroid] Add another `Activity.rxBusObservable` method takes an `ActivityEvent` parameter named `bindUntil`.

### Changed
- [RxBusAndroid] Add `@JvmStatic` annotations to public methods of `RxActivityLifecycleProviderPool`.

## [1.0.0-rc1] - 2017-03-10
### Changed
- Upagrade base `RxJava` library version from 1.1.5 to 2.0.7.
- Expose `io.reactivex.Observable` objects to make more flexible.
