using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using System.Windows.Input;
using RoomEditor.Framework;
using RoomEditor.Model;
using RoomEditor.Services;

namespace RoomEditor.ViewModel
{
    public class RoomEditorViewModel : BindableBase
    {
        protected const int RoomSize = 3;
        private RoomSet _currentRoomSet;
        private int _nextRoomId;

        private ObservableCollection<RoomSet> _roomProfiles;
        private ObservableCollection<TestRoomSet> _testRoomProfiles;
        private ObservableCollection<RoomSet> _selectedRoomProfiles;
        private TestRoomSet _testRoomSet;
        private RoomSetPatch _northRoomSetPatch;
        private RoomSetPatch _eastRoomSetPatch;
        private RoomSetPatch _westRoomSetPatch;
        private RoomSetPatch _southRoomSetPatch;

        public RoomEditorViewModel()
        {
            RoomProfiles = new ObservableCollection<RoomSet>();
            RoomProfiles.CollectionChanged += RoomProfilesOnCollectionChanged;
            TestRoomProfiles = new ObservableCollection<TestRoomSet>();
            SelectedRoomProfiles = new ObservableCollection<RoomSet>();
            SelectedRoomProfiles.CollectionChanged += SelectedItemsOnCollectionChanged;
            AddNewRoomCommand = new DelegateCommand(ExecuteAddNewRoom);
            SaveRoomSetsCommand = new DelegateCommand(ExecuteSaveRoomSets);
            LoadRoomSetsCommand = new DelegateCommand(ExecuteLoadRoomSets);
        }

        private void RoomProfilesOnCollectionChanged(object sender, NotifyCollectionChangedEventArgs notifyCollectionChangedEventArgs)
        {
            if (notifyCollectionChangedEventArgs.Action == NotifyCollectionChangedAction.Add)
            {
                foreach (var roomProfile in notifyCollectionChangedEventArgs.NewItems.OfType<RoomSet>())
                {
                    TestRoomSet testRoomSet = new TestRoomSet()
                    {
                        RoomSet = roomProfile,
                        Patches = new Dictionary<Direction, RoomSetPatch>()
                    };
                    TestRoomProfiles.Add(testRoomSet);
                }
            } else if (notifyCollectionChangedEventArgs.Action == NotifyCollectionChangedAction.Remove)
            {
                var roomSets = notifyCollectionChangedEventArgs.OldItems.OfType<RoomSet>().ToArray();

                foreach (var testRoomSet in TestRoomProfiles.Where(_ => roomSets.Contains(_.RoomSet)).ToArray())
                {
                    TestRoomProfiles.Remove(testRoomSet);
                }
            } else if (notifyCollectionChangedEventArgs.Action == NotifyCollectionChangedAction.Reset)
            {
                TestRoomProfiles.Clear();
            }
        }

        private void SelectedItemsOnCollectionChanged(object sender, NotifyCollectionChangedEventArgs notifyCollectionChangedEventArgs)
        {
            CurrentRoomSet = notifyCollectionChangedEventArgs.NewItems.OfType<RoomSet>().FirstOrDefault();
        }

        public ICommand AddNewRoomCommand { get; set; }
        public ICommand SaveRoomSetsCommand { get; set; }
        public ICommand LoadRoomSetsCommand { get; set; }

        public ObservableCollection<RoomSet> RoomProfiles
        {
            get => _roomProfiles;
            private set => SetField(ref _roomProfiles, value);
        }

        public ObservableCollection<RoomSet> SelectedRoomProfiles
        {
            get => _selectedRoomProfiles;
            set => SetField(ref _selectedRoomProfiles, value);
        }

        public ObservableCollection<TestRoomSet> TestRoomProfiles
        {
            get => _testRoomProfiles;
            private set => SetField(ref _testRoomProfiles, value);
        }

        private List<RoomSetPatch> Patches { get; set; } = new List<RoomSetPatch>();

        public RoomSetPatch NorthRoomSetPatch
        {
            get => TestRoomSet?.Patches[Direction.North];
        }

        public RoomSetPatch EastRoomSetPatch
        {
            get => TestRoomSet?.Patches[Direction.East];
        }

        public RoomSetPatch WestRoomSetPatch
        {
            get => TestRoomSet?.Patches[Direction.West];
        }

        public RoomSetPatch SouthRoomSetPatch
        {
            get => TestRoomSet?.Patches[Direction.South];
        }

        public RoomSet CurrentRoomSet
        {
            get => _currentRoomSet;
            set
            {
                var previousValue = _currentRoomSet;
                if (SetField(ref _currentRoomSet, value))
                {
                    if (previousValue != null)
                    {
                        foreach (var roomProfileRoom in previousValue.Rooms)
                            roomProfileRoom.PropertyChanged -= OnRoomChanged;
                    }
                    if (_currentRoomSet != null)
                    {
                        foreach (var roomProfileRoom in _currentRoomSet.Rooms)
                            roomProfileRoom.PropertyChanged += OnRoomChanged;
                    }

                    foreach (var testRoomProfile in TestRoomProfiles)
                    {
                        testRoomProfile.Patches = new Dictionary<Direction, RoomSetPatch>
                        {
                            {Direction.North, GetPatchedRoom(Direction.North, value, _testRoomSet?.RoomSet)},
                            {Direction.East, GetPatchedRoom(Direction.East, value, _testRoomSet?.RoomSet)},
                            {Direction.South, GetPatchedRoom(Direction.South, value, _testRoomSet?.RoomSet)},
                            {Direction.West, GetPatchedRoom(Direction.West, value, _testRoomSet?.RoomSet)},
                        };
                    }

                    /*//TestRoomProfiles.Clear();
                    foreach (var roomProfile in RoomProfiles)
                    {
                        

                        TestRoomSet testRoomSet = new TestRoomSet()
                        {
                            RoomSet = roomProfile,
                            Patches = 
                        };
                        TestRoomProfiles.Add(testRoomSet);
                        if (roomProfile == value && TestRoomSet == null)
                        {
                            TestRoomSet = testRoomSet;
                        }
                    }*/
                    
                    UpdateRoomConnections();
                }
            }
        }

        public TestRoomSet TestRoomSet
        {
            get => _testRoomSet;
            set
            {
                if (SetField(ref _testRoomSet, value))
                    UpdateRoomConnections();
            }
        }

        public bool IsNorthSideValid => NorthSideConnections.Any(_ => _);
        public bool IsSouthSideValid => SouthSideConnections.Any(_ => _);
        public bool IsEastSideValid => EastSideConnections.Any(_ => _);
        public bool IsWestSideValid => WestSideConnections.Any(_ => _);

        public IEnumerable<bool> NorthSideConnections => GetConnections(Direction.North, CurrentRoomSet, TestRoomSet);
        public IEnumerable<bool> SouthSideConnections => GetConnections(Direction.South, CurrentRoomSet, TestRoomSet);
        public IEnumerable<bool> EastSideConnections => GetConnections(Direction.East, CurrentRoomSet, TestRoomSet);
        public IEnumerable<bool> WestSideConnections => GetConnections(Direction.West, CurrentRoomSet, TestRoomSet);


        private IEnumerable<bool> GetConnections(Direction direction, RoomSet currentRoomSet,TestRoomSet testRoomSet)
        {
            var currentRoomOffsets = direction.GetPrimaryRoomOffset(RoomSize);
            var testRoomOffsets = direction.GetTestRoomOffset(RoomSize);

            var patchSet = testRoomSet?.Patches[direction];

            return Enumerable.Range(0, RoomSize)
                .Select(i => 
                IsRoomEnabled(currentRoomSet, currentRoomOffsets.xOffset(i), currentRoomOffsets.zOffset(i)) && 
                (IsRoomEnabled(testRoomSet?.RoomSet, testRoomOffsets.xOffset(i), testRoomOffsets.zOffset(i)) ||
                 IsRoomEnabled(patchSet, testRoomOffsets.xOffset(i), testRoomOffsets.zOffset(i))));
        }

        private RoomSetPatch GetPatchedRoom(Direction direction, RoomSet currentRoomSet, RoomSet testRoomSet)
        {
            if (testRoomSet == null || currentRoomSet == null) return null;
            var roomSetPatch = Patches.SingleOrDefault(_ =>
                (_.AffectedRoomId == currentRoomSet.Id && _.ConditionRoomId == testRoomSet.Id &&
                 _.Direction == direction) ||
                (_.AffectedRoomId == testRoomSet.Id && _.ConditionRoomId == currentRoomSet.Id &&
                 _.Direction == direction.Opposite()));
            if (roomSetPatch == null)
            {
                roomSetPatch = new RoomSetPatch
                {
                    AffectedRoomId = currentRoomSet.Id,
                    ConditionRoomId = testRoomSet.Id,
                    Direction = direction,
                    Rooms = Enumerable
                        .Range(0, RoomSize * RoomSize)
                        .Select(_ => new PatchedRoom(_ / RoomSize, _ % RoomSize, null))
                        .ToArray()
                };
                Patches.Add(roomSetPatch);
            }
            return roomSetPatch;
        }

        private void ExecuteAddNewRoom()
        {
            var roomId = _nextRoomId++;
            var roomProfile = new RoomSet
            {
                Id = roomId,
                Name = $"New Room {roomId}",
                Rooms = Enumerable.Range(0, RoomSize * RoomSize)
                    .Select(value => new Room(value % RoomSize, value / RoomSize, false))
                    .ToArray()
            };

            RoomProfiles.Add(roomProfile);
            CurrentRoomSet = roomProfile;
        }

        private async void ExecuteLoadRoomSets()
        {
            try
            {
                var fileService = new FileService();
                RoomProfiles.Clear();
                foreach (var roomProfile in await fileService.LoadRooms())
                {
                    RoomProfiles.Add(roomProfile);
                }
            }
            catch (Exception e)
            {
                Debug.WriteLine("Error Loading file {0}", e);
            }
        }

        private async void ExecuteSaveRoomSets()
        {
            try
            {
                var fileService = new FileService();
                await fileService.SaveRooms(RoomSize, RoomSize, RoomProfiles);
            }
            catch (Exception e)
            {
                Debug.WriteLine("Error Saving file {0}", e);
            }
        }

        private void OnRoomChanged(object sender, PropertyChangedEventArgs e)
        {
            UpdateRoomConnections();
        }
        
        private void UpdateRoomConnections()
        {
            RaisePropertyChanged(nameof(NorthSideConnections));
            RaisePropertyChanged(nameof(SouthSideConnections));
            RaisePropertyChanged(nameof(EastSideConnections));
            RaisePropertyChanged(nameof(WestSideConnections));

            RaisePropertyChanged(nameof(IsNorthSideValid));
            RaisePropertyChanged(nameof(IsSouthSideValid));
            RaisePropertyChanged(nameof(IsEastSideValid));
            RaisePropertyChanged(nameof(IsWestSideValid));

            RaisePropertyChanged(nameof(NorthRoomSetPatch));
            RaisePropertyChanged(nameof(SouthRoomSetPatch));
            RaisePropertyChanged(nameof(EastRoomSetPatch));
            RaisePropertyChanged(nameof(WestRoomSetPatch));
        }

        private bool IsRoomEnabled(RoomSet roomSet, int x, int z)
        {
            return roomSet?.Rooms[z * RoomSize + x].Present ?? false;
        }

        private bool IsRoomEnabled(RoomSetPatch roomSetPatch, int x, int z)
        {
            return roomSetPatch?.Rooms[z * RoomSize + x].Present ?? false;
        }
    }
}