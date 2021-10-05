/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrfmesh.node.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import no.nordicsemi.android.mesh.Group;
import no.nordicsemi.android.mesh.utils.AddressType;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.GroupCallbacks;
import no.nordicsemi.android.nrfmesh.R;
import no.nordicsemi.android.nrfmesh.adapter.AddressTypeAdapter;
import no.nordicsemi.android.nrfmesh.adapter.GroupAdapterSpinner;
import no.nordicsemi.android.nrfmesh.databinding.DialogFragmentGroupSubscriptionBinding;
import no.nordicsemi.android.nrfmesh.utils.HexKeyListener;
import no.nordicsemi.android.nrfmesh.utils.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static no.nordicsemi.android.mesh.utils.AddressType.ALL_FRIENDS;
import static no.nordicsemi.android.mesh.utils.AddressType.ALL_NODES;
import static no.nordicsemi.android.mesh.utils.AddressType.ALL_PROXIES;
import static no.nordicsemi.android.mesh.utils.AddressType.ALL_RELAYS;
import static no.nordicsemi.android.mesh.utils.AddressType.GROUP_ADDRESS;
import static no.nordicsemi.android.mesh.utils.AddressType.UNICAST_ADDRESS;

public class DialogFragmentHeartbeatDestination extends DialogFragment {

    private static final String DST = "DST";
    private static final String GROUPS = "GROUPS";
    private static final String GROUP = "GROUP";
    private ArrayList<Group> mGroups = new ArrayList<>();
    private static final AddressType[] ADDRESS_TYPES = {UNICAST_ADDRESS, GROUP_ADDRESS, ALL_PROXIES, ALL_FRIENDS, ALL_RELAYS, ALL_NODES};

    private DialogFragmentGroupSubscriptionBinding binding;

    private AddressTypeAdapter mAddressTypeAdapter;
    private Group mGroup;

    public static DialogFragmentHeartbeatDestination newInstance(final int dst, @NonNull final ArrayList<Group> groups) {
        final DialogFragmentHeartbeatDestination fragmentPublishAddress = new DialogFragmentHeartbeatDestination();
        final Bundle args = new Bundle();
        args.putInt(DST, dst);
        args.putParcelableArrayList(GROUPS, groups);
        fragmentPublishAddress.setArguments(args);
        return fragmentPublishAddress;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        binding = DialogFragmentGroupSubscriptionBinding.inflate(getLayoutInflater());

        int address = 0;
        if (getArguments() != null) {
            address = getArguments().getInt(DST);
            mGroups = getArguments().getParcelableArrayList(GROUPS);
        }
        if (savedInstanceState != null) {
            mGroup = savedInstanceState.getParcelable(GROUP);
        } else {
            mGroup = ((GroupCallbacks) requireActivity()).createGroup();
        }

        setAddressType(address);
        binding.summary.setText(R.string.dialog_summary_heartbeat_destination);
        binding.addressTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                updateAddress(mAddressTypeAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {

            }
        });

        binding.groupContainer.radioSelectGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.groupNameLayout.setEnabled(!isChecked);
            binding.groupNameLayout.setError(null);
            binding.groupAddressLayout.setEnabled(!isChecked);
            binding.groupAddressLayout.setError(null);
            binding.groupContainer.groups.setEnabled(isChecked);
            binding.groupContainer.radioCreateGroup.setChecked(!isChecked);
        });

        binding.groupContainer.radioCreateGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.groupNameLayout.setEnabled(isChecked);
            binding.groupNameLayout.setError(null);
            binding.groupAddressLayout.setEnabled(isChecked);
            binding.groupAddressLayout.setError(null);
            binding.groupContainer.groups.setEnabled(!isChecked);
            binding.groupContainer.radioSelectGroup.setChecked(!isChecked);
        });

        final GroupAdapterSpinner adapter = new GroupAdapterSpinner(mGroups);
        binding.groupContainer.groups.setAdapter(adapter);

        if (mGroups.isEmpty()) {
            binding.groupContainer.radioSelectGroup.setEnabled(false);
            binding.groupContainer.groups.setEnabled(false);
            binding.groupContainer.radioCreateGroup.setChecked(true);
        } else {
            binding.groupContainer.radioSelectGroup.setChecked(true);
            binding.groupContainer.radioCreateGroup.setChecked(false);
        }

        final KeyListener hexKeyListener = new HexKeyListener();
        binding.addressInput.setKeyListener(hexKeyListener);
        binding.addressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                mGroup = null;
                if (TextUtils.isEmpty(s.toString())) {
                    binding.groupAddressLayout.setError(getString(R.string.error_empty_group_address));
                } else {
                    binding.groupAddressLayout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {

            }
        });

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext()).
                setIcon(R.drawable.ic_lan_24dp).
                setTitle(R.string.destination_address).
                setView(binding.getRoot()).
                setPositiveButton(R.string.ok, null).
                setNegativeButton(R.string.cancel, null);

        final AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> setPublishAddress());

        return alertDialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(GROUP, mGroup);
    }

    private void setPublishAddress() {
        final String input = binding.addressInput.getEditableText().toString().trim();
        final int address;
        final AddressType type = (AddressType) binding.addressTypes.getSelectedItem();
        switch (type) {
            default:
            case UNICAST_ADDRESS:
                if (validateInput(input)) {
                    address = Integer.parseInt(input, 16);
                    ((DestinationAddressCallbacks) requireActivity())
                            .onDestinationAddressSet(address);
                    dismiss();
                }
                break;
            case GROUP_ADDRESS:
                try {
                    if (binding.groupContainer.radioCreateGroup.isChecked()) {
                        final String name = binding.nameInput.getEditableText().toString().trim();
                        final String groupAddress = binding.addressInput.getEditableText().toString().trim();
                        if (validateInput(name, groupAddress)) {
                            if (mGroup != null) {
                                ((DestinationAddressCallbacks) requireActivity()).
                                        onDestinationAddressSet(mGroup);
                                dismiss();
                            } else {
                                if (((GroupCallbacks) requireActivity())
                                        .onGroupAdded(name, Integer.parseInt(groupAddress, 16))) {
                                    dismiss();
                                }
                            }
                        }
                    } else {
                        final Group group = (Group) binding.groupContainer.groups.getSelectedItem();
                        ((DestinationAddressCallbacks) requireActivity()).onDestinationAddressSet(group);
                        dismiss();
                    }
                } catch (IllegalArgumentException ex) {
                    binding.groupAddressLayout.setError(ex.getMessage());
                }
                break;
        }
    }

    private void setAddressType(final int address) {
        mAddressTypeAdapter = new AddressTypeAdapter(requireContext(), ADDRESS_TYPES);
        binding.addressTypes.setAdapter(mAddressTypeAdapter);
        final AddressType type = MeshAddress.getAddressType(address);
        if (type != null) {
            switch (type) {
                case UNICAST_ADDRESS:
                    binding.addressTypes.setSelection(0);
                    break;
                case GROUP_ADDRESS:
                    binding.addressTypes.setSelection(1);
                    break;
            }
        }
    }

    private void updateAddress(@NonNull final AddressType addressType) {
        switch (addressType) {
            default:
            case UNICAST_ADDRESS:
                binding.addressInput.getEditableText().clear();
                updateFixedGroupAddressVisibility(2, true);
                break;
            case GROUP_ADDRESS:
                final int index = getGroupIndex(0);
                binding.groupContainer.groups.setSelection(index);
                binding.groupAddressLayout.setEnabled(false);
                binding.groupNameLayout.setVisibility(VISIBLE);
                binding.groupContainer.getRoot().setVisibility(VISIBLE);
                binding.labelContainer.setVisibility(GONE);
                final Group group = ((GroupCallbacks) requireActivity())
                        .createGroup(binding.nameInput.getEditableText().toString().trim());
                if (group != null) {
                    binding.addressInput.setText(MeshAddress.formatAddress(group.getAddress(), false));
                }
                break;
            case ALL_PROXIES:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_PROXIES_ADDRESS, false);
                break;
            case ALL_FRIENDS:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_FRIENDS_ADDRESS, false);
                break;
            case ALL_RELAYS:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_RELAYS_ADDRESS, false);
                break;
            case ALL_NODES:
                updateFixedGroupAddressVisibility(MeshAddress.ALL_NODES_ADDRESS, false);
                break;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void updateFixedGroupAddressVisibility(final int address, final boolean enabled) {
        binding.addressInput.setText(MeshAddress.formatAddress(address, false));
        binding.groupAddressLayout.setEnabled(enabled);
        binding.groupNameLayout.setVisibility(GONE);
        binding.groupContainer.getRoot().setVisibility(GONE);
        binding.labelContainer.setVisibility(GONE);
    }

    private int getGroupIndex(final int address) {
        for (int i = 0; i < mGroups.size(); i++) {
            if (address == mGroups.get(i).getAddress()) {
                return i;
            }
        }
        return 0;
    }

    private boolean validateInput(@NonNull final String input) {

        try {
            if (input.length() % 4 != 0 || !input.matches(Utils.HEX_PATTERN)) {
                binding.groupAddressLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }
            final AddressType type = (AddressType) binding.addressTypes.getSelectedItem();

            final int address = Integer.parseInt(input, 16);
            switch (type) {
                default:
                    if (!MeshAddress.isValidUnassignedAddress(address)) {
                        binding.groupAddressLayout.setError(getString(R.string.invalid_address_value));
                        return false;
                    }
                    return true;
                case UNICAST_ADDRESS:
                    if (!MeshAddress.isValidUnicastAddress(address)) {
                        binding.groupAddressLayout.setError(getString(R.string.invalid_unicast_address));
                        return false;
                    }
                    return true;
                case GROUP_ADDRESS:
                    if (!MeshAddress.isValidGroupAddress(address)) {
                        binding.groupAddressLayout.setError(getString(R.string.invalid_group_address));
                        return false;
                    }
                    for (Group group : mGroups) {
                        if (address == group.getAddress()) {
                            binding.groupAddressLayout.setError(getString(R.string.error_group_address_in_used));
                            return false;
                        }
                    }
                    return true;
            }
        } catch (IllegalArgumentException ex) {
            binding.groupAddressLayout.setError(ex.getMessage());
            return false;
        }
    }

    private boolean validateInput(@NonNull final String name,
                                  @NonNull final String address) {
        try {
            if (TextUtils.isEmpty(name)) {
                binding.groupNameLayout.setError(getString(R.string.error_empty_group_name));
                return false;
            }
            if (address.length() % 4 != 0 || !address.matches(Utils.HEX_PATTERN)) {
                binding.groupAddressLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            final int groupAddress = Integer.valueOf(address, 16);
            if (!MeshAddress.isValidGroupAddress(groupAddress)) {
                binding.groupAddressLayout.setError(getString(R.string.invalid_address_value));
                return false;
            }

            for (Group group : mGroups) {
                if (groupAddress == group.getAddress()) {
                    binding.groupAddressLayout.setError(getString(R.string.error_group_address_in_used));
                    return false;
                }
            }
        } catch (IllegalArgumentException ex) {
            binding.groupAddressLayout.setError(ex.getMessage());
            return false;
        }

        return true;
    }
}
